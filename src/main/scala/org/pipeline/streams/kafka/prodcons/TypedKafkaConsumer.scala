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
package org.pipeline.streams.kafka.prodcons

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.errors.WakeupException
import org.slf4j._
import org.pipeline.streams.kafka.KafkaAdminClient.consumerProperties

import java.io.IOException
import java.time.Duration
import java.util._
import scala.jdk.CollectionConverters._


/**
 * Generic, typed Kafka consumer
 * The kafka consumer is responsible to keep the connectivity to Kafka service open
 * @param valueDeserializerClass Class used to deserialize the value retrieved from Kafka
 * @param kafkaTopic Topic from which messages are consumed
 * @tparam T Type of the value or payload
 *
 * @author Patrick Nicolas
 * @version 0.0.1
 */
private[streams] case class TypedKafkaConsumer[T](
  valueDeserializerClass: String,
  kafkaTopic: String) {
  import TypedKafkaConsumer._

  private[this] var isStarted = false
  private[this] val kafkaConsumer = new KafkaConsumer[String, T](consumerProperties)
  kafkaConsumer.subscribe(Collections.singletonList(kafkaTopic))


  private[this] val pollTimeDurationMs: Duration = {
    val pollTimeInterval = consumerProperties.getProperty("max.poll.interval.ms").toLong
    Duration.ofMillis(pollTimeInterval)
  }

  final def close(): Unit = kafkaConsumer.close()

  @inline
  def syncCommit(): Unit = kafkaConsumer.commitSync()

  @inline
  def asyncCommit(): Unit = kafkaConsumer.commitAsync()



  /**
   * Main loop to process event records. Topic are extracted dynamically
   */
  def receive: Seq[(String, T)] =
      // Iterate through records assuming auto-commit of offset.
    try {
      // Simple health check to start
      check()
      val consumerRecords: ConsumerRecords[String, T] = kafkaConsumer.poll(pollTimeDurationMs)
      val records = consumerRecords.records(kafkaTopic).asScala
      // If records have been detected.....
      if (records.size > 0L) {
        logger.info(s"Consume ${records.size} messages")
        records.map(record => (record.key, record.value)).toSeq
      }
      else
        Seq.empty[(String, T)]
    }
        // Manual commit of offsets...
    catch {
      case e: WakeupException =>
        logger.error(s"To wake up ${e.getMessage}")
        kafkaConsumer.wakeup()
        Seq.empty[(String, T)]

      case e: UnsatisfiedLinkError =>
        logger.error(s"Unsatisfied link ${e.getMessage}")
        e.printStackTrace()
        Seq.empty[(String, T)]

      case e: IOException =>
        logger.error(s"Native file not found ${e.getMessage}")
        e.printStackTrace()
        Seq.empty[(String, T)]

      case e: Exception =>
        logger.error(s"Generic exception ${e.getMessage}")
        e.printStackTrace()
        Seq.empty[(String, T)]
    }

  private def check(): Unit =
    if(!isStarted) {
      logger.info(s"\nReady to consume $kafkaTopic with pooling duration of ${pollTimeDurationMs.toString} msecs. .....")
      isStarted = true
    }
}


/**
 * Singleton to retrieve Consumer properties
 * @author Patrick Nicolas
 * @version 0.6
 */
private[streams] object TypedKafkaConsumer {
  val logger: Logger = LoggerFactory.getLogger("TypedKafkaConsumer")
}

