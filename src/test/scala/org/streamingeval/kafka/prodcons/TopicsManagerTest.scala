package org.streamingeval.kafka.prodcons

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.kafka.KafkaAdminClient
import org.streamingeval.kafka.serde.RequestSerDe

final class TopicsManagerTest extends AnyFlatSpec{

  it should "Succeed creating a new topic" in {
    assert(KafkaAdminClient.isAlive, "Failed to connect to Kafka")
    val newTopic = "test-responses"
    val topicsManager = TopicsManager(RequestSerDe.deserializingClass)
    val topics = topicsManager.createTopic(newTopic)

    println(s"""Current list of topics: ${topics.mkString(" ")}""")
  }
}
