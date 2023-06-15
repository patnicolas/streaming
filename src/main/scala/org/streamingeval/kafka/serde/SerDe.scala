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
package org.streamingeval.kafka.serde

import com.fasterxml.jackson.core.{JsonParseException, JsonProcessingException}
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.streamingeval.util.LocalFileUtil
import org.slf4j.{Logger, LoggerFactory}


/**
  * Generic trait for defining the package-class name for serialization and de-serialization class.
  * Serialization and deserialization uses Jackson library
  * @author Patrick Nicolas
  * @version 0.5
  */
private[streamingeval] trait SerDe {
  val serializingClass: String
  val deserializingClass: String
}


/**
  * Singleton to define the generic read (deserialization) and write (serialization) operations
  * @author Patrick Nicolas
  * @version 0.5
  */
private[streamingeval] object SerDe {
  val logger: Logger = LoggerFactory.getLogger("SerDe")

  final val serDePrefix = "org.examples.kafka.serde"

  /**
    * Read bytes from Kafka message and converted in a parameterized type
    * @param bytes Bytes associated with the message
    * @param clazz Class of the payload
    * @tparam T Type for the message
    * @return Instance of the message
    */
  def read[T](bytes: Array[Byte], clazz: Class[T]): T = try {
    LocalFileUtil.Json.mapper.readValue[T](new String(bytes), clazz)
  } catch {
    case e: UnrecognizedPropertyException =>
      logger.error(e.getMessage)
      null.asInstanceOf[T]
    case e: JsonParseException =>
      logger.error(e.getMessage)
      null.asInstanceOf[T]
    case e: JsonMappingException =>
      logger.error(e.getMessage)
      null.asInstanceOf[T]
    case e: Exception =>
      logger.error(e.getMessage)
      null.asInstanceOf[T]
  }

  def write[T](t: T): Array[Byte] = try {
    LocalFileUtil.Json.mapper.writeValueAsBytes(t)
  }
  catch {
    case e: JsonProcessingException =>
      logger.error(e.getMessage)
      Array.empty[Byte]
  }
}

