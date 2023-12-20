package org.pipeline.streams.kafka.prodcons

import org.scalatest.flatspec.AnyFlatSpec
import org.pipeline.streams.{RequestPayload, ResponsePayload}

private[prodcons] final class KafkaPredictionProcTest extends AnyFlatSpec {
  import KafkaPredictionProcTest._

  it should "Succeed Consuming from a given Kafka topic" in {
    val topic = "test-streaming"
    val topicsManager = TopicsManager()
    if (topicsManager.listTopics.contains(topic)) {
      val resultTopic = "output-streaming"
      if(!topicsManager.listTopics.contains(topic))
        topicsManager.createTopic(resultTopic)

      KafkaPredictionProc.executeBatch(topic, resultTopic, 128, bertMaskingPipeline)
    }
    else {
      val condition = false
      assert(condition, s"Topic $topic does not exist")
    }
  }
}


private[prodcons] object KafkaPredictionProcTest {

  private val bertMaskingPipeline: Seq[RequestPayload] => Seq[ResponsePayload] =
    (payloadInputs: Seq[RequestPayload]) => {
      payloadInputs.map(
        payload => {
          val words: Array[String] = payload.consumedPayload.split("\\s+")
          val maskedWords = words.indices.map(index => if(index % 7 == 0) "Mask" else words(index))
          ResponsePayload(payload.id, maskedWords.mkString(" "))
        }
      )
    }
}
