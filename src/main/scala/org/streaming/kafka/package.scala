/**
 * Copyright 2022,2023 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.streaming

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.common.serialization.{Serde, Serdes, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.StreamsConfig
import org.streaming.kafka.KafkaConfig.kafkaProdCcnfig
import org.streaming.kafka.serde.{RequestDeserializer, RequestSerializer, ResponseDeserializer, ResponseSerializer}

import java.util.Properties

/**
 * Functions and singletons shared by all Kafka related classes
 * @author Patrick Nicolas
 * @version 0.0.2
 */
package object kafka {
  val stringSerde: Serde[String] = Serdes.String()


  /**
   * Basic object to evaluate if a connection to Kafka service
   * is still alive..
   */
  object KafkaAdminClient {
        // Default Kafka properties
    lazy val consumerProperties: java.util.Properties = {
      val c = KafkaConfig.kafkaConsConfig
      val props = load(KafkaConfig.kafkaConsConfig)
      props.put("key.deserializer", classOf[StringDeserializer])
      props.put("value.deserializer", classOf[RequestDeserializer])
      props.put("key.serializer", classOf[StringSerializer])
      props.put("value.serializer", classOf[RequestSerializer])
      props
    }

    lazy val producerProperties: java.util.Properties = {
      val props = load(kafkaProdCcnfig)
      props.put("key.deserializer", classOf[StringDeserializer])
      props.put("value.deserializer", classOf[ResponseDeserializer])
      props.put("key.serializer", classOf[StringSerializer])
      props.put("value.serializer", classOf[ResponseSerializer])
      props
    }


    lazy val stringConsumerProperties: java.util.Properties = {
      val m = KafkaConfig.kafkaConsConfig
      val props = load(KafkaConfig.kafkaConsConfig)
      props.put("key.deserializer", classOf[StringDeserializer])
      props.put("value.deserializer", classOf[StringDeserializer])
      props.put("key.serializer", classOf[StringSerializer])
      props.put("value.serializer", classOf[StringSerializer])
      props
    }

    lazy val stringProducerProperties: java.util.Properties = {
      val conf = kafkaProdCcnfig
      val props = load(kafkaProdCcnfig)
      props.put("key.deserializer", classOf[StringDeserializer])
      props.put("value.deserializer", classOf[StringDeserializer])
      props.put("key.serializer", classOf[StringSerializer])
      props.put("value.serializer", classOf[StringSerializer])
      props
    }


    lazy val streamingProperties: java.util.Properties = {
      val props = load(KafkaConfig.kafkaStreamConfig)
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-function-scala-example")
      props
    }


    private def load(config: KafkaConfig): java.util.Properties = {
      config.kafkaParameters.foldLeft(new Properties()) (
        (props, param) => {
          props.put(param.key, param.value)
          props
        }
      )
    }


    /**
     * Test if the  server is available for the default (fallback Kafka properties)
     * @return
     */
    def isAlive: Boolean = isAlive(AdminClient.create(consumerProperties))

    def isAlive(adminClient: AdminClient): Boolean = {
      val listOfTopics = adminClient.listTopics()
      val futures = listOfTopics.listings()
      val result = futures.get()
      listOfTopics != null && result != null && result.size() > 0
    }
  }

  final class KafkaTopicException(msg: String) extends Exception(msg) {}
}
