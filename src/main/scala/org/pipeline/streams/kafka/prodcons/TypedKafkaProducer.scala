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

import org.apache.kafka.clients.producer._
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.{InterruptException, SerializationException, TimeoutException}
import org.slf4j._

import java.util.Properties

/**
 * Generic wrapper for Typed Kafka producer
 * @param properties Serializer for the value sent by the producer
 * @param topic Topic to which produce Kafka responses
 *
 * @author Patrick Nicolas
 * @version 0.0.1
 */
private[streams] abstract class TypedKafkaProducer[T](properties: Properties, topic: String)  {
  import TypedKafkaProducer._

  private[this] val kafkaProducer = new KafkaProducer[String, T](properties)


  /**
   * Send a wrapper to Kafka using 'round-robin' across partitions
   * @param producingMessage Wrapper to be produced to Kafka
   */
  def send(producingMessage: (String, T)): Unit = try {
    val (key, value) = producingMessage
    val producer = new ProducerRecord[String, T](topic, key, value)
    kafkaProducer.send(producer)
  }
  catch {
    case e: InterruptException =>
      logger.error(s"Producer interrupted: ${e.getMessage}")
    case e: SerializationException =>
      logger.error(s"Producer failed serialization: ${e.getMessage}")
    case e: TimeoutException =>
      logger.error(s"Producer time out: ${e.getMessage}")
    case e: KafkaException =>
      logger.error(s"Producer Kafka error: ${e.getMessage}")
  }

  def send(producingMessages: Seq[(String, T)]): Unit = producingMessages.foreach(send)

  def close(): Unit = kafkaProducer.close()
}


/**
 * Singleton to retrieve Kafka producer properties
 * @todo Get Number of partitions from
 * @author Patrick Nicolas
 * @version 0.5
 */
private[streams] object TypedKafkaProducer {
  val logger: Logger = LoggerFactory.getLogger("TypedKafkaProducer")
}
