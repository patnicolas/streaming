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
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}

import scala.collection.mutable.ListBuffer
import scala.util.Random

private[kalman] object SparkKalman {

  /**
   *
   * @param kalmanParametersSeq Set of Kalman parameters
   * @param z Series of observed measurements as dense vector
   * @param sparkSession Implicit reference to the current Spark context
   * @return Sequence of prediction from various Kalman filter
   */

  /**
   *
   * @param z Series of observed measurements as dense vector
   * @param numSamplingMethods Number of samples to be processed concurrently
   * @param minSamplingInterval Minimum number of samples to be ignored between sampling
   * @param samplingInterval Range of random sampling
   * @param sparkSession Implicit reference to the current Spark context
   * @return Sequence of Kalman filter predictions on sampled measurements
   */
  def apply(
    kalmanParams: KalmanParameters,  // Kalman parameters used by the filter/predictor
    z: Array[DenseVector],           // Series of observed measurements as dense vector
    numSamplingMethods: Int,         // Number of samples to be processed concurrently
    minSamplingInterval: Int,        // Minimum number of samples to be ignored between sampling
    samplingInterval: Int            // Range of random sampling
    )(implicit sparkSession: SparkSession): Seq[Seq[DenseVector]] = {
    import sparkSession.implicits._

      // Generate the various samples from the large set of raw measurements
    val samples: Seq[Seq[DenseVector]] = (0 until numSamplingMethods).map(
      _ => sampling(z, minSamplingInterval, samplingInterval)
    )

      // Distribute the Kalman prediction-correction cycle over Spark workers
      // by assigning a partition to a Kalman process and sampled measurement.
    val samplesDS = samples.toDS()
    val predictionsDS = samplesDS.mapPartitions(
      (sampleIterator: Iterator[Seq[DenseVector]]) => {
        val acc = ListBuffer[Seq[DenseVector]]()

        while(sampleIterator.hasNext) {
          implicit val kalmanNoise: KalmanNoise = KalmanNoise(kalmanParams.A.numRows)
          val rKalman = new RKalman(kalmanParams)
          val z = sampleIterator.next()
          acc.append(rKalman(z))
        }
        acc.iterator
       }
    ).persist()
    predictionsDS.collect()
  }


  private def sampling(
    measurements: Array[DenseVector],
    minSamplingInterval: Int,
    samplingInterval: Int): Seq[DenseVector] = {

    val rand = new Random(42L)
    val interval = rand.nextInt(samplingInterval) + minSamplingInterval
    measurements.indices.foldLeft(ListBuffer[DenseVector]())(
      (acc, index) => {
        if (index % interval == 0)
          acc.append(measurements(index))
        acc
      }
    )
  }
}
