package org.streamingeval.kafka.prodcons

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.kafka.prodcons.TopicsManager.AdminClientState
import org.streamingeval.{RequestMessage, RequestPayload}
import org.streamingeval.util.LocalFileUtil

private[prodcons] final class TypedKafkaGeneratorTest extends AnyFlatSpec {
  import TypedKafkaGeneratorTest._

  it should "Succeed producing kafka messages" in {
    AdminClientState.start()
    val topic = "test-requests"
    // generate the messages
    val generatedMessages = generateMessages
    // Produce messages to Kafka
    val kafkaProducer = new TypedKafkaGenerator[RequestMessage](topic)
    kafkaProducer.send(generatedMessages)
    AdminClientState.close()
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