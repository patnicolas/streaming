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

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer




/**
 * Implementation of the Discrete Kalman filter
 * @param initialKalmanParams Initial parameters for the State and Measurement equations
 * @param kalmanNoise Pair (Process, Measurement) noises
 * @author Patrick Nicolas
 */
private[kalman] final class RKalman(
  initialKalmanParams: KalmanParameters
)(implicit kalmanNoise: KalmanNoise){
  private[this] var kalmanParams: KalmanParameters = initialKalmanParams

  @inline
  def getKalmanParams: KalmanParameters = kalmanParams

  @inline
  def getState: DenseVector = kalmanParams.x


  def getNumRows: Int = kalmanParams.A.numRows

  /**
   * Recursive implementation of the Kalman filter
   * @param z Series of observed measurements
   * @return List of predictions as dense vector
   */
  def apply(z: Array[DenseVector]): List[DenseVector] = {
    @tailrec
    def execute(
      z: Array[DenseVector],
      index: Int,
      predictions: ListBuffer[DenseVector]): List[DenseVector] = {
        if (index >= z.length)  // Criteria to end recursion
          predictions.toList
        else {
          val nextX = predict()
          val nextZ: DenseVector = kalmanParams.H.multiply(nextX)
          predictions.append(nextZ)
          update(z(index))
            // Execute the next measurement points
          execute(z, index + 1, predictions)
        }
    }

    execute(z, 0, ListBuffer[DenseVector]())
  }

  /**
   * Implement the prediction of the state variable
   * x(t+1) = A.x(t) + B.u(t) + Q
   * P(t+1) = A.P(t)A^T^ + Q
   * @param U Optional control vector
   * @return New, updated estimation
   */
  def predict(U: Option[DenseVector] = None): DenseVector = {
    // Compute the first part of the state equation S = A.x
    val newX = kalmanParams.A.multiply(kalmanParams.x)

    // Add the control matrix if u is provided  S += B.u
    val correctedX = U.map(u => kalmanParams.B.multiply(u)).getOrElse(newX)

    // Update the error covariance matrix P as P(t+1) = A.P(t).A_transpose + Q
    val newP = add(
      kalmanParams.A.multiply(kalmanParams.P).multiply(kalmanParams.ATranspose),
      kalmanNoise.processNoise
    )
    // Update the kalman parameters
    kalmanParams = kalmanParams.copy(x = correctedX, P = newP)
    kalmanParams.x
  }

  /**
   * Implement the update of the state x and error covariance P while computing the Kalman gain
   * @param z Measurement dense vector
   * @return Kalman gain dense matrix
   */
  def update(z: DenseVector): DenseMatrix = {
    val y = kalmanParams.measureDiff(z)
    val S = kalmanParams.computeS( kalmanNoise.measureNoise)

    val kalmanGain: DenseMatrix = kalmanParams.gain(S)
    kalmanParams = kalmanParams.copy(x = add(kalmanParams.x, kalmanGain.multiply(y)))
    kalmanParams = kalmanParams.copy(P = updateErrorCovariance(kalmanGain))

    kalmanGain
  }

  /**
   * Implement the update of the state x and error covariance P while computing the Kalman gain
   * @param z Measurement as a array
   * @return Kalman gain as an array of arrays
   */
  def update(z: Array[Double]): Array[Array[Double]] = {
    val zVec = new DenseVector(z)
    val kalmanGain = update(zVec)
    kalmanGain.values
    Array.tabulate(zVec.size)(i => Array.tabulate(zVec.size)(j => kalmanGain(i, j)))
  }

  private def updateErrorCovariance(kalmanGain: DenseMatrix): DenseMatrix = {
    val identity = DenseMatrix.eye(kalmanGain.numRows)
    val kHP = subtract(identity, kalmanGain.multiply(kalmanParams.H)).multiply(kalmanParams.P)
    val kH = subtract(identity, kalmanGain.multiply(kalmanParams.H).transpose)
    val kR = (kalmanGain.multiply(kalmanNoise.measureNoise)).multiply(kalmanGain.transpose)
    add(kHP.multiply(kH), kR)
  }

}



private[pipeline] object RKalman {

  val nullVector = new DenseVector(Array.empty[Double])
  private def isVectorNull(v: DenseVector): Boolean = v.size == 0
}
