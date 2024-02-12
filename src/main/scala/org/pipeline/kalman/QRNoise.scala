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

import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector}


/**
 * Wrapper for process and measurement noises
 *
 * @param qNoise mean of process noise
 * @param rNoise mean of the measurement noise
 * @param length Size of the process and measurement noise vectors
 * @param noiseGen Noise generator function from a given value
 * @author Patrick Nicolas
 */
case class QRNoise(qNoise: Double, rNoise: Double, length: Int, noiseGen: Double => Double) {
  final def processNoise: DenseMatrix =
    new DenseMatrix(length, length, Array.fill(length*length)(noiseGen(qNoise)))
  final def measureNoise: DenseMatrix =
    new DenseMatrix(length, length, Array.fill(length*length)(noiseGen(qNoise)))
}