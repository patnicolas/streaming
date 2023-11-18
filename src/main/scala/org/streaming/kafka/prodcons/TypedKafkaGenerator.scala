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
package org.streaming.kafka.prodcons

import org.streaming.kafka.KafkaAdminClient.consumerProperties

/**
 * Generator of request messages simulating client to consumer
 * @param topic Topic to which produce Kafka responses
 * @tparam T Type of request messages
 *
 * @author Patrick Nicolas
 * @version 0.0.3
 */
private[streaming] final class TypedKafkaGenerator[T](topic: String)
  extends TypedKafkaProducer[T](consumerProperties, topic)
