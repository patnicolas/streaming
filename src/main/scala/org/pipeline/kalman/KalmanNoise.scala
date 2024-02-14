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

import org.apache.spark.ml.linalg.DenseMatrix


/**
 * Wrapper for process (Q matrix) and measurement (R Matrix) noises
 *
 * @param qNoise Standard deviation of process noise
 * @param rNoise Standard deviation of the measurement noise
 * @param length Size of the process and measurement noise vectors
 * @param noiseGen Noise generator function from a given value
 * @author Patrick Nicolas
 */
private[kalman] case class KalmanNoise(
  qNoise: Double,          // Standard deviation of process noise
  rNoise: Double,          // Standard deviation of the measurement noise
  length: Int,             // Number of features or rows associated with the noise
  noiseGen: Double => Double) {  // Distribution function for generating noise
  final def processNoise: DenseMatrix =
    new DenseMatrix(length, length, randMatrix(length, qNoise, noiseGen).flatten)
  final def measureNoise: DenseMatrix =
    new DenseMatrix(length, length, Array.fill(length*length)(noiseGen(qNoise)))
}


/**
 * Companion object for dedicated constructors
 */

private[kalman] object KalmanNoise {
  import org.apache.commons.math3.distribution.NormalDistribution
  import scala.util.Random

  /**
   * Constructor for Kalman Gaussian noises
   * @param qNoise Mean of process noise
   * @param rNoise mean of the measurement noise
   * @param length Size of the process and measurement noise vectors
   * @return Instance of Kalman noise
   */
  def apply(qNoise: Double, rNoise: Double, length: Int): KalmanNoise =
    new KalmanNoise(qNoise, rNoise, length, normalRandomValue)

  /**
   * Constructor for Kalman Normal (mean = 0.0) noise
   * @param length Size of the process and measurement noise vectors
   * @return Instance of Kalman noise
   */
  def apply(length: Int): KalmanNoise =
    new KalmanNoise(1.0, 1.0, length, normalRandomValue)


  private def normalRandomValue(stdDev: Double): Double =
    new NormalDistribution(0.0, stdDev).density(Random.nextDouble)
}

