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
import org.streaming.ResponseMessage

import java.util
import org.streaming.kafka.serde.SerDe.serDePrefix


/**
  * Serializer for the prediction response
  * @see org.mlops.nlp.medical.MedicalCodeTypes
  * @author Patrick Nicolas
  * @version 0.5
  */
private[streaming] final class ResponseSerializer extends Serializer[ResponseMessage] {
  override def serialize(topic: String, predictResp: ResponseMessage): Array[Byte] =
    SerDe.write[ResponseMessage](predictResp)

  override def close(): Unit = {}
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = { }
}


/**
  * Deserializer for the response
  * @author Patrick Nicolas
  * @version 0.0.1
  */
private[streaming] final class ResponseDeserializer extends Deserializer[ResponseMessage] {
  override def deserialize(topic: String, bytes: Array[Byte]): ResponseMessage =
    SerDe.read[ResponseMessage](bytes, classOf[ResponseMessage])

  override def close(): Unit = {}
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = { }
}


/**
  * Singleton for serializing and deserializing classes for response messages
  * @author Patrick Nicolas
  * @version 0.0.1
  */
private[streaming] object ResponseSerDe extends SerDe {
  override val serializingClass = s"${serDePrefix}.ResponseSerializer"
  override val deserializingClass = s"${serDePrefix}.ResponseDeserializer"
}
