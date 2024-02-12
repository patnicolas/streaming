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
 * Wrapper for Kalman parameters for the State and Measurement equations
 *     x_hat = A.x + B.u + process_noise     (x_hat_
 *     z = H.P + measurement noise
 *
 * @param A  State transition matrix
 * @param B  Control matrix (optional)
 * @param H  Measurement matrix
 * @param P  Error covariance matrix
 * @param x  Estimated value vector
 *
 * @author Patrick Nicolas
 */
private[kalman] case class KalmanParameters(
  A: DenseMatrix,     // State transition dense matrix
  B: DenseMatrix,     // Optional Control dense matrix
  H: DenseMatrix,     // Measurement dense matrix
  P: DenseMatrix,     // Error covariance dense matrix
  x: DenseVector) {   // Estimated value dense vector

  private lazy val HTranspose: DenseMatrix = H.transpose

  lazy val ATranspose: DenseMatrix = A.transpose

  /**
   * Compute the difference z = H.x
   * @param z Measurement dense vector
   * @return Difference between estimated measurement and actual measurement
   */
  def measureDiff(z: DenseVector): DenseVector = subtract(z, H.multiply(x))

  /**
   * Compute H.P.H[T] + measurement noise
   * @param measureNoise Measurement or device noise
   * @return Estimated error covariance
   */
  def computeS(measureNoise: DenseMatrix): DenseMatrix =
    add(H.multiply(P).multiply(HTranspose), measureNoise)

  def innovation(z: DenseVector): DenseVector = subtract(z, H.multiply(x))

  /**
   * Compute the Kalman gain, given an estimated state S  P.H[T]*S[-1]
   *           P * H_transpose * S_inverse
   * @param S Estimated state
   * @return Kalman gain
   */
  def gain(S: DenseMatrix): DenseMatrix = {
    val bzStateMatrix = new breeze.linalg.DenseMatrix(S.numRows,S.numCols, S.values)
    val invBzStateMatrix = breeze.linalg.inv(bzStateMatrix)
    val invStateMatrix = new DenseMatrix(S.numCols, S.numRows, invBzStateMatrix.toArray)

    P.multiply(HTranspose).multiply(invStateMatrix)
  }
}


/**
 * Companion object for dedicated constructors
 */

private[kalman] object KalmanParameters {
  def apply(
    A: Array[Double],
    B: Array[Double],
    H: Array[Double],
    P: Array[Double],
    x: Array[Double]): KalmanParameters = {
    val nRows = A.length >> 1
    new KalmanParameters(
      new DenseMatrix(nRows, nRows, A),
      new DenseMatrix(nRows, nRows, B),
      new DenseMatrix(nRows, nRows, H),
      new DenseMatrix(nRows, nRows, P),
      new DenseVector(x)
    )
  }


  def apply(
    A: Array[Array[Double]],
    B: Array[Array[Double]],
    H: Array[Array[Double]],
    P: Array[Array[Double]], x: Array[Double])
  : KalmanParameters = {
    val nRows = A.length
    val nCols = A.head.length
    new KalmanParameters(
      new DenseMatrix(nRows, nCols, A.flatten),
      new DenseMatrix(nRows, nCols, B.flatten),
      new DenseMatrix(nRows, nCols, H.flatten),
      new DenseMatrix(nRows, nCols, P.flatten), new DenseVector
      (x))
  }
}
