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
package org.streamingeval.kafka.prodcons

import org.streamingeval.kafka.KafkaAdminClient.producerProperties

/**
 * Production of result of computation or transform to Kafka
 *
 * @param topic Topic to which produce Kafka responses
 * @tparam T Type of producing message
 * @author Patrick Nicolas
 * @version 0.0.3
 */
private[streamingeval] final class TypedKafkaResult[T](topic: String)
  extends TypedKafkaProducer[T](producerProperties, topic)
