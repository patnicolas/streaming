package org.streamingeval.spark

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.InputDStream
import org.scalatest.flatspec.AnyFlatSpec

private[spark] final class SparkDStreamsTest extends AnyFlatSpec {
  import SparkDStreamsTest._

  it should "process Kafka streams" in {
    val streamsFromKafka = createStreamFromKafka
    val checkpointDir = "~/temp"
    val sparkDStreams = new SparkDStreams(streamsFromKafka, 1500L,Some(checkpointDir))
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

  private val countWords: InputDStream[ConsumerRecord[String, String]] => Unit =
    (stream: InputDStream[ConsumerRecord[String, String]]) => {

      def reduceFunction(wordPair: (String, Int), anotherWordPair: (String, Int)): (String, Int) =
        "total" -> (wordPair._2 + anotherWordPair._2)

      def inverseReduceFunction(wordPair: (String, Int), anotherWordPair: (String, Int)): (String, Int) =
        "total" -> (wordPair._2 - anotherWordPair._2)

      val wordPairs = stream.map(record => (record.value(), 1))
      // A more efficient implementation using inverse function
      val efficientTotalWordCount = wordPairs.reduceByWindow(reduceFunction, inverseReduceFunction, Seconds(30), Seconds(15))
      // Built-in functionality
      wordPairs.countByWindow(Seconds(30), Seconds(15)).print()
    }

}
