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
package org.streamingeval

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.common.serialization.{Serde, Serdes}

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

    lazy val KafkaProperties: java.util.Properties = {
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:9092")
      props.put("request.timeout.ms", 2000)
      props.put("connections.max.idle.ms", 3000)
      props
    }

    def isAlive: Boolean = isAlive(AdminClient.create(KafkaProperties))

    def isAlive(adminClient: AdminClient): Boolean = {
      val nodes = adminClient.describeCluster().nodes().get()
      nodes != null && nodes.size() > 0
    }
  }
}
