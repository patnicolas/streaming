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
package org.streamingeval.spark

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import java.io.IOException
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j._
import scala.util.Try

/**
 * Spark configuration file thqt distinguish between dynamic and static parameters
 * @param sparkParameters List of Spark parameters definition
 *
 * @author Patrick Nicolas
 * @version 0.0.2
 */
private[streamingeval] case class SparkConfiguration(sparkParameters: Seq[ParameterDefinition])
  extends TuningParameters[SparkConfiguration] {
  require(sparkParameters.nonEmpty, "Spark configuration parameters are undefined")
  /**
   * Extracts the tunable parameters
   * @return List of dynamic parameters
   */
  override def getTunableParams: Seq[ParameterDefinition] = sparkParameters.filter(_.isDynamic)
  override def toString: String = sparkParameters.map(_.toString).mkString("\n")
}




private[streamingeval] final object SparkConfiguration {
  import org.streamingeval.util.LocalFileUtil._

  final val log: Logger = LoggerFactory.getLogger("SparkConfiguration")

  private final val mlSparkConfigFile = "conf/sparkConfig.json"

  final val mlSparkConfig: SparkConfiguration = try {
    val content = Load.local(fsFilename = mlSparkConfigFile)
    content.map( Json.mapper.readValue(_, classOf[SparkConfiguration])).getOrElse(
      throw new IllegalStateException("Spark dynamic configuration improperly loaded")
    )
  } catch {
    case e: JsonMappingException =>
      throw new IllegalStateException(s"Failed to map configuration parameters ${e.getMessage}")
    case e: JsonParseException =>
      throw new IllegalStateException(s"Failed to parse configuration file ${e.getMessage}")
    case e: IOException =>
      throw new IllegalStateException(s"Failed to find configuration file ${e.getMessage}")
  }

  /**
   * Update the existing Spark configuration
   * @param sparkConf Current Spark configuration
   * @return Updated Spark configuration
   */
  final def buildConf: SparkConf =
    mlSparkConfig.sparkParameters.foldLeft(new SparkConf)(
      (conf, param) => {
        conf.set(param.key, param.value)
        conf
      }
    )

  /**
   * Main factory for the execution context created from a configuration file
   * @return Execution context initialized by a configuration file
   */
  def loadSpark: Try[SparkConf] = Try { SparkConfiguration.buildConf }

  /**
   * Implicit conversion from a configuration file to a SparkSession
   * @param confFile Spark configuration file
   * @return SparkSession instance
   */
  def confToSessionFromFile: SparkSession =
    loadSpark
      .map(conf => SparkSession.builder().appName("ExecutionContext").config(conf).getOrCreate())
      .getOrElse(
        throw new IllegalStateException(s"Cannot initialize Spark configuration from conf/sparkConfig.json")
      )
}



