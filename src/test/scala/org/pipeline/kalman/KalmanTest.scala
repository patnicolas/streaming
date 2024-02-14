package org.pipeline.kalman

import org.apache.spark.ml.linalg.DenseMatrix
import org.scalatest.flatspec.AnyFlatSpec
import org.pipeline.kalman.KalmanUtil._

private[kalman] final class KalmanTest extends AnyFlatSpec{

  it should "Succeed computing inverse of a matrix" in {
    val matrix = Array[Array[Double]]( Array[Double](1.2, 2.0), Array[Double](0.4, 0.8))
    val denseMatrix = new DenseMatrix(matrix.length, matrix.length, matrix.flatten)
    val invDenseMatrix = inv(denseMatrix)
    println(invDenseMatrix)
  }
}
