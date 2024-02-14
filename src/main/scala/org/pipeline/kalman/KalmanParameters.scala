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
import org.pipeline.kalman.KalmanUtil._


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

  private def HTranspose: DenseMatrix = H.transpose

  def ATranspose: DenseMatrix = A.transpose

  /**
   * Compute the difference z = H.x
   * @param z Measurement dense vector
   * @return Difference between estimated measurement and actual measurement
   */
  def residuals(z: DenseVector): DenseVector = subtract(z, H.multiply(x))

  /**
   * Compute H.P.H[T] + measurement noise
   * @param measureNoise Measurement or device noise
   * @return Estimated error covariance
   */
  def innovation(measureNoise: DenseMatrix): DenseMatrix =
    add(H.multiply(P).multiply(HTranspose), measureNoise)


  /**
   * Compute the Kalman gain, given an estimated state S  P.H[T]*S[-1]
   *           P * H_transpose * S_inverse
   * @param S Innovation value for this iteration
   * @return Kalman gain
   */
  def gain(S: DenseMatrix): DenseMatrix = {
    val invStateMatrix = inv(S)
    P.multiply(HTranspose).multiply(invStateMatrix)
  }
}


/**
 * Companion object for dedicated constructors
 */
private[kalman] object KalmanParameters {
  /**
   * Constructor for the Kalman parameters with matrices and vectors defined as arrays
   * @param A State transition single array
   * @param B Control single array (optional)
   * @param H Measurement single array
   * @param P Error covariance single array
   * @param x Estimated value single array
   * @return Instance of KalmanParameters
   */
  def apply(
    A: DVector,
    B: DVector,
    H: DVector,
    P: DVector,
    x: DVector): KalmanParameters = {
    val nRows = A.length >> 1
    new KalmanParameters(
      new DenseMatrix(nRows, nRows, A),
      new DenseMatrix(nRows, nRows, B),
      new DenseMatrix(nRows, nRows, H),
      new DenseMatrix(nRows, nRows, P),
      new DenseVector(x)
    )
  }

  /**
   * Constructor for the Kalman parameters with matrices and vectors defined as arrays or array
   *
   * @param A State transition 2-dimension array
   * @param B Control 2-dimension array
   * @param H Measurement 2-dimension array
   * @param P Error covariance 2-dimension array
   * @param x Estimated value single array
   * @return Instance of KalmanParameters
   */
  def apply(
    A: DMatrix,
    B: DMatrix,
    H: DMatrix,
    P: DMatrix,
    x: DVector): KalmanParameters = {
    val nRows = A.length
    val nCols = A.head.length
    new KalmanParameters(
      new DenseMatrix(nRows, nCols, A.flatten),
      new DenseMatrix(nRows, nCols, B.flatten),
      new DenseMatrix(nRows, nCols, H.flatten),
      new DenseMatrix(nRows, nCols, P.flatten), new DenseVector(x))
  }

  def apply(
    A: DMatrix,
    B: Option[DMatrix],
    H: DMatrix,
    P: Option[DMatrix],
    x: DVector): KalmanParameters = {
    val nRows = A.length
    val nCols = A.head.length
    val b = B.getOrElse(identityMatrix(nRows))
    val p = P.getOrElse((identityMatrix(nRows)))

    new KalmanParameters(
      new DenseMatrix(nRows, nCols, A.flatten),
      new DenseMatrix(nRows, nCols, b.flatten),
      new DenseMatrix(nRows, nCols, H.flatten),
      new DenseMatrix(nRows, nCols, p.flatten),
      new DenseVector(x))
  }


  /**
   * Simplified constructors with no Control or initial error convariance matrices
   * @param A  State transition 2-dimension array
   * @param H  measurement 2-dimension array
   * @param x  Estimated value single array
   * @return Instance of KalmanParameters
   */

  def apply(
    A: DMatrix,
    H: DMatrix,
    x: DVector): KalmanParameters = KalmanParameters(A, None, H, None, x)
}
