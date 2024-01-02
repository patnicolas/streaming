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
package org.pipeline.streams

import org.apache.spark.sql.{Dataset, SparkSession}


package object spark {
  /**
   * Default entry for architecture (Kafka, Spark) parameters
   *
   * @param key       Native name of the parameter
   * @param value     Typed value of the parameter
   * @param isDynamic Is parameter tunable
   * @param paramType Type of parameter (Int, String, Double,....)
   * @param range     Range of possible, valid values
   * @author Patrick Nicolas
   * @version 0.0.3
   */
  case class ParameterDefinition(
    key: String,
    value: String,
    isDynamic: Boolean,
    paramType: String,
    range: Seq[String] = Seq.empty[String]) {
    override def toString: String =
      s"$key $value ${if (isDynamic) "dynamic" else "static"}, $paramType, ${range.mkString(" ")}"

    /**
     * Retrieve the range of valid values from the configuration if the range is not empty
     * @param numValues Number of valid values for this parameters
     * @throws IllegalStateException if the parameter type is not supported
     * @return Sequence of permitted value for this Spark parameter
     */
    @throws(clazz = classOf[IllegalStateException])
    def getRange(numValues: Int): Seq[AnyVal] = {
      if(range.nonEmpty)
        paramType match {
        case "Int" =>
          if(numValues == -1) range.map(_.toInt)
          else {
            val min = range.head.toInt
            val max = range.last.toInt
            (min until max)
          }
        case "Float" =>
          if(numValues == -1) range.map(_.toFloat)
          else {
            val min = range.head.toFloat
            val max = range.last.toFloat
            val step = (max- min)/numValues
            (0 until numValues).map(index => min + index*step)
          }
        case "Boolean" =>
          Seq[Boolean](false, true)
        case _ =>
          throw new IllegalStateException(s"Parameter type: $paramType is not supported")
      }
      else
        Seq.empty[AnyVal]
    }
  }


  /**
   * Define tuning parameters
   */
  trait TuningParameters[T <: TuningParameters[T]] {
    def getTunableParams: Seq[ParameterDefinition]
  }

  /**
   * Optimized parameterized join of two data sets
   *
   * @param tDS First input data set
   * @param tDSKey key for first data set
   * @param uDS Second input data set
   * @param uDSKey key for Second data set
   * @tparam T Type of elements of the first data set
   * @tparam U Type of elements of the second data set
   * @return Data set of pair (T, U)
   */
  final def sortingJoin[T, U](
    tDS: Dataset[T],
    tDSKey: String,
    uDS: Dataset[U],
    uDSKey: String
  )(implicit sparkSession: SparkSession): Dataset[(T, U)] = {
    val sortedTDS = tDS.repartition(tDS(tDSKey)).sortWithinPartitions(tDS(tDSKey)).cache()
    val sortedUDS = uDS.repartition(uDS(uDSKey)).sortWithinPartitions(uDS(uDSKey)).cache()
    sortedTDS.joinWith(
      sortedUDS,
      sortedTDS(tDSKey) === sortedUDS(uDSKey),
      joinType = "inner"
    )
  }
}
