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
package org.pipeline.streams.kafka.prodcons


/**
  * Generic Kafka procedure consuming messages of type REQ and producing messages of type RESP
  * @tparam REQ Type of request or input to the pipeline
  * @tparam RESP Type of response or output from the pipeline
  *
  * @author Patrick Nicolas
  * @version 0.0.1
  */
private[kafka] trait KafkaProc[REQ, RESP] {
  val consumer: TypedKafkaConsumer[REQ]
  val producer: TypedKafkaProducer[RESP]
  val transform: Seq[REQ] => Seq[RESP]
  /**
    * Close consumer and producer of this pipeline
    */
  def close(): Unit = {
    consumer.close()
    producer.close()
  }
}
