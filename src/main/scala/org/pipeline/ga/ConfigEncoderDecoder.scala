/**
 * Copyright 2022,2024 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.pipeline.ga

import org.pipeline.streams.spark.SparkConfiguration
import org.pipeline.streams.spark.SparkConfiguration.mlSparkConfig

import scala.collection.mutable.ListBuffer


/**
 * Wrapper object for encoding/decoding a Spark configuration
 * @author Patrick Nicolas
 */

private[ga] object ConfigEncoderDecoder{

  /**
   * Convert the current Spark configuration parameters into a Chromosome
   * @param sparkConfig Spark configuration as a list of dynamic parameters
   * @return Chromosome associated with the current Spark configuration
   */
  def encode(sparkConfig: SparkConfiguration): Chromosome[Int, Float] = {
    val floatGenes = ListBuffer[Gene[Float]]()
    val intGenes = ListBuffer[Gene[Int]]()

    // Walk through the dynamic parameters of the current Spark configuration
    sparkConfig.sparkParameters.foreach(paramValue => {
      val value = paramValue.value
      val cleansedParamValue: String =
        if (!value.last.isDigit) value.substring(0, value.length - 1)
        else value

      // The type of encoder and gene depends on the type of configuration parameter
      paramValue.paramType match {
        case "Int" =>
          val gaEncoder = new GAEncoderInt(encodingLength = 6, paramValue.range.map(_.toInt))
          val intGene = Gene[Int](paramValue.key, cleansedParamValue.toInt, gaEncoder)
          intGenes.append(intGene)

        case "Float" =>
          val gaEncoder = new GAEncoderFloat(
            encodingLength = 6,
            scaleFactor = 1.0F,
            paramValue.range.map(_.toFloat)
          )
          val floatGene = Gene[Float](paramValue.key, cleansedParamValue.toFloat, gaEncoder)
          floatGenes.append(floatGene)
        case _ =>
      }
    })
    Chromosome[Int, Float](intGenes, floatGenes)
  }

  /**
   * Convert a chromosome into a Spark configuration (sequence of dynamic parameters)
   * @param chromosome Chromosome generated through GA
   * @return Spark configuration
   */
  def decode(chromosome: Chromosome[Int, Float]): SparkConfiguration = {
    val intGenes = chromosome.getFeatures1
    val floatGenes = chromosome.getFeatures2
    val sparkDynaParamsMap = mlSparkConfig.sparkParameters.map(param => (param.key, param)).toMap

    val intSparkParams = intGenes.map(
      gene => {
        val param = sparkDynaParamsMap.getOrElse(
          gene.getId,
          throw new GAException(s"Gene identifier ${gene.getId} not found")
        )
        param.copy(value = gene.getValue.toString)
      }
    )

    val floatSparkParams = floatGenes.map(
      gene => {
        val param = sparkDynaParamsMap.getOrElse(
          gene.getId,
          throw new GAException(s"Gene identifier ${gene.getId} not found")
        )
        param.copy(value = gene.getValue.toString)
      }
    )
    new SparkConfiguration(intSparkParams ++ floatSparkParams)
  }
}
