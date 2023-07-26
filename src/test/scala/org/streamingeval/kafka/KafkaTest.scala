package org.streamingeval.kafka

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.kafka.KafkaConfig.kafkaProdCcnfig

import scala.collection.mutable.ListBuffer

private[kafka] final class KafkaTest extends AnyFlatSpec {

  it should "Succeed loading Kafka configuration for consumer" in {
    val propertiesIterator = KafkaAdminClient.consumerProperties.entrySet().iterator()
    val acc = new StringBuffer("Consumer configuration\n")
    while(propertiesIterator.hasNext) {
      acc.append(propertiesIterator.next()).append("\n")
    }
    println(acc.toString)
  }

  it should "Succeed loading tunable Kafka configuration for producer" in {
    val tunableProducerParams = kafkaProdCcnfig.getTunableParams.map(param => s"${param.key}\t${param.value}")
    println(s"Producer tunable params -----\n${tunableProducerParams.mkString("\n")}")
  }

  it should "Succeed loading Kafka configuration for producer" in {
    val propertiesIterator = KafkaAdminClient.producerProperties.entrySet().iterator()
    val acc = new StringBuffer("\n\nProducer configuration\n")
    while (propertiesIterator.hasNext) {
      acc.append(propertiesIterator.next()).append("\n")
    }
    println(acc.toString)
  }

  it should "Succeed loading Kafka configuration for streaming" in {
    val propertiesIterator = KafkaAdminClient.streamingProperties.entrySet().iterator()
    val acc = new StringBuffer("Streaming configuration\n")
    while (propertiesIterator.hasNext) {
      acc.append(propertiesIterator.next()).append("\n")
    }
    println(acc.toString)
  }
}
