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
package org.streaming.kafka.serde

import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import org.streaming.RequestMessage

import java.util
import org.streaming.kafka.serde.SerDe.serDePrefix



/**
  * Serializer for request messages
  * @author Patrick Nicolas
  * @version 0.0.1
  */
final class RequestSerializer extends Serializer[RequestMessage] {
  override def serialize(topic: String, request: RequestMessage): Array[Byte] = {
    val content = SerDe.write[RequestMessage](request)
    content
  }

  override def close(): Unit = { }

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = { }
}


/**
  * Deserializer for the prediction request
  * @author Patrick Nicolas
  * @version 0.0.1
  */
private[streaming] final class RequestDeserializer extends Deserializer[RequestMessage] {

  override def deserialize(topic: String, bytes: Array[Byte]): RequestMessage =
    SerDe.read[RequestMessage](bytes, classOf[RequestMessage])

  override def close(): Unit = { }

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = { }
}




/**
  * Singleton for serializing and deserializing classes for response
  * @author Patrick Nicolas
  * @version 0.0.1
  */
private[streaming] object RequestSerDe extends SerDe {
  override val serializingClass = s"$serDePrefix.RequestSerializer"
  override val deserializingClass = s"$serDePrefix.RequestDeserializer"
}