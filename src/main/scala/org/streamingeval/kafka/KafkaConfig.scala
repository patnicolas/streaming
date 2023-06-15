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
package org.streamingeval.kafka


import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import org.streamingeval.util.LocalFileUtil.{Json, Load}
import org.streamingeval.{ParameterDefinition, TuningParameters}
import org.slf4j.{Logger, LoggerFactory}

import java.io.IOException


/**
 * Container for dynamic Kafka configuration from a local file conf/kafkaConfig.json
 * @param kafkaParameters Kafka typed parameters loaded from Configuration file
 * @throws IllegalArgumentException If no parameters are found in the configuration file
 *
 * @author Patrick Nicolas
 * @version 0.3.1.0
 */
private[streamingeval] case class KafkaConfig(kafkaParameters: Seq[ParameterDefinition]) extends TuningParameters[KafkaConfig] {
  require(kafkaParameters.nonEmpty, "MlKafkaConfig is empty")

  override def getTunableParams: Seq[ParameterDefinition] = kafkaParameters.filter(_.isDynamic)
  override def toString: String = kafkaParameters.map(_.toString).mkString("\n")
}


private[streamingeval] object KafkaConfig {
  final val logger: Logger = LoggerFactory.getLogger("KafkaConfig")

  private final val mlKafkaConfigFile = "conf/kafkaConfig.json"
  private final val kafkaConsumerConfigurationMarker = "kafkaConsumerConfiguration%"
  private final val kafkaProducerConfigurationMarker = "kafkaProducerConfiguration%"

  /**
   * Instantiate the dynamic Kafka configuration
   * {{{
   *   Step 1: Load the parameters
   *   Step 2: Strip the type of parameters (producer or consumer) from the content of the configuration file
   *   Step 3: Instantiate the Kafka configuration
   * }}}
   */
  private val kafkaConfig: KafkaConfig = try {
    val content = Load
      .local(fsFilename = mlKafkaConfigFile)
      .map(stripParamCategory(_, kafkaConsumerConfigurationMarker))
      .map(stripParamCategory(_, kafkaProducerConfigurationMarker))

    content.map( Json.mapper.readValue(_, classOf[KafkaConfig])).getOrElse(
      throw new IllegalStateException("Kafka dynamic configuration improperly loaded")
    )
  } catch {
    case e: JsonParseException =>
      throw new IllegalStateException(s"Failed to parse configuration file ${e.getMessage}")
    case e: JsonMappingException =>
      throw new IllegalStateException(s"Failed to map configuration parameters ${e.getMessage}")
    case e: IOException =>
      throw new IllegalStateException(s"Failed to find configuration file ${e.getMessage}")
  }

  /**
   * Retrieve the value associated with a Kafka dynamic parameter
   * @param key Official name of the Kafka parameter
   * @param elseValue Default value
   * @return Value of the Kafka parameters defined in the configuration file
   */
  def getParameterValue(key: String, elseValue: String): String = parametersMap.getOrElse(key, elseValue)


  private def stripParamCategory(content: String, categoryMarker: String): String =
    content.replace(categoryMarker, "")

  final private val parametersMap = kafkaConfig.kafkaParameters.map(param => (param.key, param.value)).toMap
}





