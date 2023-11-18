package org.streaming.spark

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.InputDStream
import org.scalatest.flatspec.AnyFlatSpec
import org.streaming.RequestMessage
import org.streaming.kafka.prodcons.TopicsManager
import org.streaming.kafka.prodcons.TopicsManager.AdminClientState

private[spark] final class SparkDStreamsTest extends AnyFlatSpec {
  import SparkDStreamsTest._
  import org.streaming.spark.implicits.sparkSession

  it should "process Kafka streams" in {
    AdminClientState.start()

    val topic = "test-streaming"
    val topicsManager = TopicsManager()
    println(s"Current list of topics: ${topicsManager.listTopics.mkString(" ")}")

    val streamsFromKafka = createStreamFromKafka
    val checkpointDir = "~/temp"
    val sparkDStreams = new SparkDStreams[RequestMessage](streamsFromKafka, 1500L,Some(checkpointDir))
    sparkDStreams(countWords)
  }
}

private[spark] object SparkDStreamsTest {
  private def createStreamFromKafka: StreamFromKafka = {
    val topics = "test"
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "spark_test_group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    StreamFromKafka(Array[String](topics), kafkaParams)
  }

  private val countWords: InputDStream[ConsumerRecord[String, RequestMessage]] => Unit =
    (stream: InputDStream[ConsumerRecord[String, RequestMessage]]) => {

      def reduceFunction(wordPair: (String, Int), anotherWordPair: (String, Int)): (String, Int) =
        "total" -> (wordPair._2 + anotherWordPair._2)

      def inverseReduceFunction(wordPair: (String, Int), anotherWordPair: (String, Int)): (String, Int) =
        "total" -> (wordPair._2 - anotherWordPair._2)

      // Extract word pairs from text
      val wordPairs = stream.flatMap(record => {
        val content: String = record.value().requestPayload.consumedPayload
        content.split(",").map((_, 1))
      })

      // A more efficient implementation using inverse function
      val efficientTotalWordCount = wordPairs.reduceByWindow(reduceFunction, inverseReduceFunction, Seconds(30), Seconds(15))
      // Built-in functionality
      wordPairs.countByWindow(Seconds(30), Seconds(15)).print()
    }
}
