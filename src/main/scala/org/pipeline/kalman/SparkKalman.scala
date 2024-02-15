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

package org.pipeline.kalman

import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

private[kalman] object SparkKalman {

  /**
   *
   * @param kalmanParametersSeq Set of Kalman parameters
   * @param z Series of observed measurements as dense vector
   * @param sparkSession Implicit reference to the current Spark context
   * @return Sequence of prediction from various Kalman filter
   */
  def apply(
    kalmanParametersSeq: Seq[KalmanParameters],
    z: Array[DenseVector]
  )(implicit sparkSession: SparkSession): Seq[List[DenseVector]] = {
    import sparkSession.implicits._

    val kalmanParamsDS = kalmanParametersSeq.toDS()

    val predictionsDS = kalmanParamsDS.mapPartitions(
      (kalmanParamsIterator: Iterator[KalmanParameters]) => {
        val acc = ListBuffer[List[DenseVector]]()

        while(kalmanParamsIterator.hasNext) {
          val kalmanParams = kalmanParamsIterator.next()

          implicit val kalmanNoise: KalmanNoise = KalmanNoise(kalmanParams.A.numRows)
          val rKalman = new RKalman(kalmanParams)
          acc.append(rKalman(z))
        }
        acc.iterator
      }
    ).persist()

    predictionsDS.collect()
  }
}
