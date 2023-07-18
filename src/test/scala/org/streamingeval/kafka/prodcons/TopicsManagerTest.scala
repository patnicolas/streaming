package org.streamingeval.kafka.prodcons

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.kafka.KafkaAdminClient
import org.streamingeval.kafka.serde.RequestSerDe

private[prodcons] final class TopicsManagerTest extends AnyFlatSpec{

  ignore should "Succeed creating a new topic" in {
    assert(KafkaAdminClient.isAlive, "Failed to connect to Kafka")
    val newTopic = "test-responses"
    val topicsManager = TopicsManager()
    val topics = topicsManager.createTopic(newTopic)

    println(s"""Current list of topics: ${topics.mkString(" ")}""")
  }

  it should "Succeed listing existing topics" in {
    assert(KafkaAdminClient.isAlive, "Failed to connect to Kafka")

    val topicsManager = TopicsManager()
    println(s"List of topics:  ${topicsManager.listTopics.mkString(" ")}")
  }
}
