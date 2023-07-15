/**
  * Copyright 2022,2023 Patrick R. Nicolas. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
  * with the License. A copy of the License is located at
  *
  * http://aws.amazon.com/apache2.0/
  *
  * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
  */
package org.streamingeval.kafka.prodcons

import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.{SerializationException, TimeoutException}
import org.apache.spark.SparkException
import org.streamingeval.kafka.prodcons.KafkaPredictionProc.{RequestKeyMap, logger}
import org.streamingeval.kafka.serde.{RequestSerDe, ResponseSerDe}
import org.streamingeval.{RequestMessage, RequestPayload, ResponseMessage, ResponsePayload}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.concurrent.TrieMap


/**
  * Request handler for Kafka consumer. RequestMessage andResponseMessage are wrappers for
  * PRequest and PResponse respectively
  * @param consumer Kafka consumer associated with the Prediction request
  * @param producer Kafka producer associated with the Prediction response
  * @param transform Single transaction transformation of Kafka message
  * @see https://agilesde.atlassian.net/wiki/spaces/DS/pages/2419195989/Kafka+interfaces
  *
  * @author Patrick Nicolas
  * @version 0.0.1
  */
private[streamingeval] final class KafkaPredictionProc private(
  override val consumer: TypedKafkaConsumer[RequestMessage],
  override val producer: TypedKafkaResult[ResponseMessage],
  override val transform: Seq[RequestMessage] => Seq[ResponseMessage]
) extends KafkaProc[RequestMessage, ResponseMessage] {
  private[this] val keyEncounterPredictMap = new RequestKeyMap

  protected[this] def initEncounterKeyMap(requestMessages: Seq[(String, RequestMessage)]): Unit =
    keyEncounterPredictMap += requestMessages

  protected[this] def getKeyFromEncounter(
    responseMessages: Seq[ResponseMessage]
  ): Seq[(String, ResponseMessage)] =
    responseMessages.map(
      responseMessage => {
        val key = keyEncounterPredictMap.getKey(responseMessage.responsePayload.id)
        (key, responseMessage)
      }
    )

  protected[this] def clear: Unit = keyEncounterPredictMap.clear

  def process(requestMessages: Seq[RequestMessage]): Seq[ResponseMessage] =
    if(requestMessages.nonEmpty) {
      logger.debug(s"Process ${requestMessages.map(_.requestPayload.id).mkString(" ")}")
      val responses = transform(requestMessages)
      responses
    }
    else {
      logger.error(s"Requests from Kafka are undefined")
      Seq.empty[ResponseMessage]
    }
}


/**
  * Singleton for specialized constructors
  */
private[streamingeval] object KafkaPredictionProc {
  val logger: Logger = LoggerFactory.getLogger("KafkaPredictionProc")

  final private val maxEncounterKeyMap = 4098
  final private val requestKeyMap = new RequestKeyMap

  val predictionPipeline: Seq[RequestPayload] => Seq[ResponsePayload] =
    (requestPayload: Seq[RequestPayload]) => {
      Seq[ResponsePayload](ResponsePayload("0", "Response"))
    }


  /**
    * Class that manage matching the request with response using
    * the request id as key. The map is defined as {request.id, RequestMessage} pairs
    * This operation is thread safe.
    */
  final class RequestKeyMap {
    private[this] val requestKeyMap = TrieMap[String, String]()

    def += (requestMessages: Seq[(String, RequestMessage)]): Unit =
      requestMessages.foreach {
        case (key, requestMessage) =>
          requestKeyMap.put(requestMessage.requestPayload.id, key)
      }

    def getKey(requestId: String): String =
      requestKeyMap.getOrElse(
        requestId,
        throw new IllegalStateException(s"Request id $requestId has no matching key")
      )

    def clear(): Unit = if(requestKeyMap.size > maxEncounterKeyMap) requestKeyMap.clear
  }


  def apply(
    consumer: TypedKafkaConsumer[RequestMessage],
    producer: TypedKafkaResult[ResponseMessage],
    transform: Seq[RequestMessage] => Seq[ResponseMessage]): KafkaPredictionProc =
    new KafkaPredictionProc(consumer, producer, transform)


  /**
    * Constructor with output S3 bucket and folder and direct execution of prediction pipeline
    * @param consumeTopic Topic for prediction requests
    * @param produceTopic Topic for prediction responses
    * @param executionPipeline Execution of prediction pipeline (<=> Virtual coder)
    * @return Instance of Kafka prediction handler
    */
  def apply(
    consumeTopic: String,
    produceTopic: String,
    executionPipeline: Seq[RequestPayload] => Seq[ResponsePayload]): KafkaPredictionProc = {

    // Constructs the transform of Kafka messages for prediction
    val transform = (requestMsg: Seq[RequestMessage]) => {
      // Invoke the execution of the pipeline
      val predictions = executionPipeline(requestMsg.map(_.requestPayload))
      // If status = 0, 1, 2, ... from virtual coder then HTTP 200 statue
      predictions.map(
        predictResp =>
          ResponseMessage(System.currentTimeMillis(), 200, "200",predictResp)
      )
    }
    // Build the Kafka consumer for prediction request
    val consumer = new TypedKafkaConsumer[RequestMessage](RequestSerDe.deserializingClass, consumeTopic)
    // Build the Kafka producer for prediction response
    val producer = new TypedKafkaResult[ResponseMessage](produceTopic)
    new KafkaPredictionProc(consumer, producer, transform)
  }




  /**
    * Execute a batch of messages consumed from Kafka queue if the execution Mode, executors.executionMode is set
    * to 'kafka-spark'
    * {{{
    * Exceptions to be handled:
    *   - SparkException
    *   - IllegalStateException
    *   - InterruptedException
    *   - SerializationException
    *   - TimeoutException
    *   - KafkaException
    *
    *  Data flow:
    *   Step 1: Consume a batch of Kafka message (prediction or feedback requests)
    *   Step 2: Apply the transformation (execute pipeline)
    *   Step 3: Produce Kafka message (prediction and feedback response)
    *   Step 4: Commit the offsets
    * }}}
    *
    * @param consumeTopic Topic for consuming messages from (requests)
    * @param produceTopic Topic to produce messages to (response)
    * @param maxNumResponses Maximum number of responses (-1 for no limit)
    */
  def executeBatch(
    consumeTopic: String,
    produceTopic: String,
    maxNumResponses: Int,
    executionPipeline: Seq[RequestPayload] => Seq[ResponsePayload]): Int = {
    val kafkaHandler = KafkaPredictionProc(consumeTopic, produceTopic, executionPipeline)
    var counter = 0

    while((maxNumResponses == -1 || counter < maxNumResponses)) {
      // Pool the request topic (has its own specific Kafka exception handler)
      val consumerRecords = kafkaHandler.consumer.receive
      println(s"Retrieve ${consumerRecords.size} records")
      if (consumerRecords.nonEmpty) {
        // Generate and apply transform to the batch
        val start = System.currentTimeMillis()
        logger.info(s"Start processing ${consumerRecords.size} records")

     //   keyEncounterPredictMap += consumerRecords
        try {   // Exception related to processing
          val input: Seq[RequestMessage] = consumerRecords.map(_._2)
          val responses = kafkaHandler.process(input)
          if(responses.nonEmpty) {
            counter += responses.size
            // Produce to the output topic
            val respMessages = responses.map(
              response => {
                //       val key = keyEncounterPredictMap.getKey(response.payload.id)
                (response.responsePayload.id, response)
              }
            )
            printResponseMessage(respMessages)
            val duration = System.currentTimeMillis - start
            logger.info(s"Executed in $duration average: ${duration * 0.001 / responses.size} secs total count: $counter")

            kafkaHandler.producer.send(respMessages)
            // It is assumed nothing goes wrong after this point
            kafkaHandler.consumer.asyncCommit()
          }
          else
            logger.error("No response is produced to Kafka")
    //      keyEncounterPredictMap.clear
        }
        catch {
          case e: SparkException =>
            logger.error(s"Spark failure: ${e.getMessage}")
          case e: IllegalStateException =>
            e.printStackTrace()
            logger.error(s"Illegal state: ${e.getMessage}")
          case e: InterruptedException =>
            logger.error(s"Producer interrupted: ${e.getMessage}")
          case e: SerializationException =>
            logger.error(s"Producer failed serialization: ${e.getMessage}")
          case e: TimeoutException =>
            logger.error(s"Producer time out: ${e.getMessage}")
          case e: KafkaException =>
            logger.error(s"Producer Kafka error: ${e.getMessage}")
          case e: IndexOutOfBoundsException =>
            e.printStackTrace()
            logger.error(s"Indexed out of bounds: ${e.getMessage}")
          case e: Exception =>
            e.printStackTrace()
            logger.error(s"Undefined Kafka error: ${e.getMessage}")
        }
      }
      else
        logger.debug("No input to consumed")
    }
    logger.warn(s"Exit Kafka prediction handler and close $consumeTopic after $counter messages")
    kafkaHandler.close()
    counter
  }


  /**
    * Process the prediction request messages and produce prediction response message with the sam key
    * @param requestMessages Sequence of prediction request messages
    * @return Sequence of predict response messages
    */
  def process(requestMessages: Seq[RequestMessage]): Seq[ResponseMessage] =
    if(requestMessages.nonEmpty) {
      /*
      val responsesSeq = requestHandler.apiProcess(requestMessages.map(_.requestPayload))
      val responseMsgs = responsesSeq.indices.map(index => convert2ResponseMessage(responsesSeq(index)))
      logger.info(s"Sent ${responseMsgs.size} message")
      responseMsgs

       */
      Seq.empty[ResponseMessage]
    }
    else
      Seq.empty[ResponseMessage]


  private def convert2ResponseMessage(responsePayload: ResponsePayload): ResponseMessage =
    ResponseMessage(200, "", responsePayload)


  private def printResponseMessage(responseMessages: Seq[(String, ResponseMessage)]): Unit =
    if(responseMessages.nonEmpty) {
      val responseDump = responseMessages.map{
        case (id, response) => s"$id:${response.responsePayload.toString}"
      }
      logger.info(s"\nResponses\n${responseDump.mkString("\n")}\n---------------------------\n")
    }

}
