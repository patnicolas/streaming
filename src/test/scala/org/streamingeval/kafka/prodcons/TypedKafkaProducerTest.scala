package org.streamingeval.kafka.prodcons

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.{RequestMessage, RequestPayload}
import org.streamingeval.kafka.serde.RequestSerDe
import org.streamingeval.util.LocalFileUtil

private[prodcons] final class TypedKafkaProducerTest extends AnyFlatSpec {
  import TypedKafkaProducerTest._

  it should "Succeed producing kafka messages" in {
    val topic = "test-streaming"
    // Create the topic if it does not exists
    val topicsManager = TopicsManager()
    if(!topicsManager.listTopics.contains(topic))
      topicsManager.createTopic(topic, numPartitions = 2)
    println(s"Current list of topics: ${topicsManager.listTopics.mkString(" ")}")

    // generate the messages
    val generatedMessages = generateMessages
    // Produce messages to Kafka
    val kafkaProducer = new TypedKafkaProducer[RequestMessage](RequestSerDe.serializingClass, topic)
    generatedMessages.indices.foreach(
      index => kafkaProducer.send((index.toString, generatedMessages(index)))
    )
  }
}


private[prodcons] object TypedKafkaProducerTest {
  private def generateMessages: Seq[RequestMessage] = {
    val contents = Array[String](
      "input/note1.txt", "input/note3.txt", "input/note4.txt"
    ).flatMap(LocalFileUtil.Load.local(_))
      .map(_.replaceAll("\n", " ").replaceAll("\r", ""))
    contents.indices.map(
      index => RequestMessage(System.currentTimeMillis(), RequestPayload(index.toString, contents(index)))
    )
  }
}