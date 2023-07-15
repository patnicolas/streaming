package org.streamingeval.kafka.prodcons

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.{RequestMessage, RequestPayload}
import org.streamingeval.util.LocalFileUtil

private[prodcons] final class TypedKafkaGeneratorTest extends AnyFlatSpec {
  import TypedKafkaGeneratorTest._

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
    val kafkaProducer = new TypedKafkaGenerator[RequestMessage](topic)
    kafkaProducer.send(generatedMessages)
  }
}


private[prodcons] object TypedKafkaGeneratorTest {
  private def generateMessages: Seq[(String, RequestMessage)] = {
    val contents = Array[String](
      "input/note1.txt", "input/note3.txt", "input/note4.txt", "input/note3.txt", "input/note1.txt"
    ).flatMap(LocalFileUtil.Load.local)
      .map(_.replaceAll("\n", " ").replaceAll("\r", ""))
    (0 until 25).map(
      index => {
        val relIndex = index % contents.length
        val key = index.toString
        val msg = RequestMessage(System.currentTimeMillis(), RequestPayload(key, contents(relIndex)))
        (key, msg)
      }
    )
  }
}