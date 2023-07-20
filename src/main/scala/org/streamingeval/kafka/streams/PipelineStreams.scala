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
package org.streamingeval.kafka.streams

import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.streamingeval.kafka.streams.PipelineStreams.getProperties
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import java.util.Properties

/**
 * Basic pipeline streams that consumes requests of type T
 * @param valueDeserializerClass Class or type used in the deserialization for Kafka consumer
 * @tparam T Type of Kafka message consumed
 * @see org.streamingeval.kafka.streams.PipelineStreams
 *
 * @author Patrick Nicolas
 * @version 0.0.1
 */
private[kafka] abstract class PipelineStreams[T](valueDeserializerClass: String) {
  protected[this] val properties: Properties = getProperties
  protected[this] val streamBuilder: StreamsBuilder = new StreamsBuilder

  /**
   * Generic processing (Consuming/Producing)
   * @param requestTopic Input topic for request (Prediction or Feedback)
   * @param responseTopic Output topic for response
   */
  def start(requestTopic: String, responseTopic: String): Unit =
    for {
      topology <- createTopology(requestTopic, responseTopic)
    } yield {
      val streams = new KafkaStreams(topology, properties)
      streams.cleanUp()
      streams.start()
      print(s"Streaming for $requestTopic requests and $responseTopic responses started")
      val delayMs = 2000L
      delay(delayMs)

      sys.ShutdownHookThread {
        streams.close(Duration.ofSeconds(12))
      }
    }

  private def delay(delayMs: Long): Unit = try {
    Thread.sleep(delayMs)
  } catch {
    case e: InterruptedException => println(s"ERROR: ${e.getMessage}")
  }

  protected[this] def createTopology(inputTopic: String, outputTopic: String): Option[Topology]
}





private[kafka] object PipelineStreams {
  val logger: Logger = LoggerFactory.getLogger("PipelineStreams")

  /**
   * Load the properties from the resource file
   * @return Optional properties
   */
  def getProperties: Properties = {
    val p = new Properties()
    p.put(
      StreamsConfig.APPLICATION_ID_CONFIG,
      "map-function-scala-example"
    )
    val bootstrapServers = "localhost:9092"
    p.put(
      StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
      bootstrapServers
    )
    p
    /*
    consumerProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "AI-ML")
    consumerProperties

     */
  }
}


