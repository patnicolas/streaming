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

import scala.collection.mutable.ListBuffer

object SparkDynamicParams{


  /**
   *
   * @param sparkConfig
   * @tparam T
   * @tparam U
   * @return */
  def apply(sparkConfig: SparkConfiguration): Chromosome[Int, Double] = {
    val floatGenes = ListBuffer[Gene[Double]]()
    val intGenes = ListBuffer[Gene[Int]]()

    sparkConfig.sparkParameters.foreach(paramValue => {
      val value = paramValue.value
      val cleansedParamValue: String = if (!value.last.isDigit) value.substring(0, value.length - 1) else value

      paramValue.paramType match {
        case "Int" =>
          val quantizer = new QuantizerInt(encodingLength = 6, maxValue = 10)
          val intGene = Gene[Int](paramValue.key, cleansedParamValue.toInt, quantizer)
          intGenes.append(intGene)

        case "Float" =>
          val quantizer = new QuantizerDouble(
            encodingLength = 6,
            scaleFactor = 1.0,
            maxValue = 120.0
          )
          val floatGene = Gene[Double](paramValue.key, cleansedParamValue.toDouble, quantizer)
          floatGenes.append(floatGene)
        case _ =>
      }
    })
    Chromosome[Int, Double](intGenes, floatGenes)
  }

  def unapply(chromosome: Chromosome[Int, Double]): SparkConfiguration = ???
}
