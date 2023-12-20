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
package org.pipeline.streams.kafka.streams

import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.{Consumed, Produced}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}
import org.pipeline.streams.kafka.serde.{RequestDeserializer, RequestSerDe, RequestSerializer, ResponseDeserializer, ResponseSerializer}
import org.pipeline.streams.kafka.stringSerde
import org.pipeline.streams.{RequestMessage, RequestPayload, ResponseMessage, ResponsePayload}
import org.slf4j.{Logger, LoggerFactory}


/**
 * Specialized pipeline ot process request of type RequestMessage and produces response of type ResponseMessage
 * @param proc Transformation function
 *
 * @author Patrick Nicolas
 * @version 0.0.1
 */
private[streams] final class RequestsStreams(proc: RequestPayload => ResponsePayload)
  extends PipelineStreams[RequestMessage](RequestSerDe.deserializingClass) {
  import RequestsStreams._

  override protected[this] def createTopology(requestTopic: String, responseTopic: String): Option[Topology] = try {
    // Invoke the implicit for Consumed[String, PredictRequestMessage]
    val requestMessages: KStream[String, RequestMessage] = streamBuilder.stream[String, RequestMessage](requestTopic)
    val responseValues: KStream[String, ResponseMessage] = requestMessages.mapValues(
      requestMessage => {
        val responsePayload = proc(requestMessage.requestPayload)
        ResponseMessage(System.currentTimeMillis(),
          200,
          "",
          responsePayload)
      }
    )
    responseValues.to(responseTopic)
    Some(streamBuilder.build)
  }
  catch {
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }

}

/**
 * Singleton for Request streams that define the Serialization/Deserialization
 * of request and response messages
 */
private[streams] object RequestsStreams {
  private val logger: Logger = LoggerFactory.getLogger("RequestsStreams")

  private val requestMessageSerde: Serde[RequestMessage] = Serdes.serdeFrom(
    new RequestSerializer,
    new RequestDeserializer
  )
  implicit val consumed: Consumed[String, RequestMessage] = Consumed.`with`(stringSerde, requestMessageSerde)


  // Generate implicit for produced type associated with the Predict Response message
  private val responseMessageSerde: Serde[ResponseMessage] = Serdes.serdeFrom(
    new ResponseSerializer,
    new ResponseDeserializer
  )
  implicit val produced: Produced[String, ResponseMessage] = Produced.`with`(stringSerde, responseMessageSerde)
}
