package org.streamingeval.kafka.streams

import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.streamingeval.initialProperties
import org.streamingeval.kafka.streams.PipelineStreams.getProperties
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import java.util.Properties


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
  val logger: Logger = LoggerFactory.getLogger("KafkaPipelineStreams")

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


