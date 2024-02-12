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

import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrices, Matrix, Vector, Vectors}

import DKalman._



/**
 *
 * @param initialKalmanParams
 * @param qrNoise
 */
private[kalman] final class DKalman(
  initialKalmanParams: KalmanParameters
)(implicit qrNoise: QRNoise){
  private[this] var kalmanParams: KalmanParameters = initialKalmanParams

  @inline
  def getKalmanParams: KalmanParameters = kalmanParams

  @inline
  def getEstimate: DenseVector = kalmanParams.x

  /**
   *
   * @param u
   * @return
   */
  def predict(u: DenseVector = nullVector): Unit = {
    val newX = kalmanParams.A.multiply(kalmanParams.x)
    val correctedX =
      if(isVectorNull(u)) add(newX, kalmanParams.B.multiply(u))
      else newX

    val newP = add(
      kalmanParams.A.multiply(kalmanParams.P).multiply(kalmanParams.ATranspose),
      qrNoise.processNoise
    )
    kalmanParams = kalmanParams.copy(x = correctedX, P = newP)
  }

  def update(z: DenseVector): DenseMatrix = {
    val y = kalmanParams.measureDiff(z)
    val S = kalmanParams.computeS( qrNoise.measureNoise)

    val kalmanGain: DenseMatrix = kalmanParams.gain(S)
    kalmanParams = kalmanParams.copy(x = add(kalmanParams.x, kalmanGain.multiply(y)))
    kalmanParams = kalmanParams.copy(P = updateErrorCovariance(kalmanGain))

    kalmanGain
  }

  private def updateErrorCovariance(kalmanGain: DenseMatrix): DenseMatrix = {
    val identity = DenseMatrix.eye(kalmanGain.numRows)
    val kHP = subtract(identity, kalmanGain.multiply(kalmanParams.H)).multiply(kalmanParams.P)
    val kH = subtract(identity, kalmanGain.multiply(kalmanParams.H).transpose)
    val kR = (kalmanGain.multiply(qrNoise.measureNoise)).multiply(kalmanGain.transpose)
    add(kHP.multiply(kH), kR)
  }

}



private[pipeline] object DKalman {

  val nullVector = new DenseVector(Array.empty[Double])
  private def isVectorNull(v: DenseVector): Boolean = v.size == 0
}
