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
import org.streamingeval.initialProperties
import org.streamingeval.kafka.streams.PipelineStreams.getProperties
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import java.util.Properties

/**
 * Basic pipeline streams that consumes requests of type T
 * @param valueDeserializerClass Class or type used in the deserialization for Kafka consumer
 * @tparam T Type of Kafka message consumed
 *
 * @author Patrick Nicolas
 * @version 0.0.1
 */
private[kafka] abstract class PipelineStreams[T](valueDeserializerClass: String) {
  protected[this] val properties: Option[Properties] = getProperties(valueDeserializerClass)
  protected[this] val streamBuilder: StreamsBuilder = new StreamsBuilder

  /**
   * Generic processing (Consuming/Producing)
   * @param requestTopic Input topic for request (Prediction or Feedback)
   * @param responseTopic Output topic for response
   */
  def start(requestTopic: String, responseTopic: String): Unit =
    for {
      topology <- createTopology(requestTopic, responseTopic)
      prop <- properties
    } yield {
      val streams = new KafkaStreams(topology, prop)
      streams.start()
      sys.ShutdownHookThread {
        streams.close(Duration.ofSeconds(12))
      }
    }

  protected[this] def createTopology(inputTopic: String, outputTopic: String): Option[Topology]
}





private[kafka] object PipelineStreams {
  val logger: Logger = LoggerFactory.getLogger("PipelineStreams")

  /**
   * Load the properties from the resource file
   * @param valueDeserializerClass Class for the deserialization of the consumer
   * @return Optional properties
   */
  def getProperties(valueDeserializerClass: String): Option[Properties] =
    initialProperties.map(
      props => {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "AI_ML")
        props
      }
    )
}


