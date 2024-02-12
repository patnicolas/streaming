package org.pipeline.kalman

import org.scalatest.flatspec.AnyFlatSpec
import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrices, Matrix, Vector, Vectors}

private[kalman] final class DKalmanTest extends AnyFlatSpec{

    it should "succeed operating on dense vectors and matrices" in {
      val input: Array[Double] = Array[Double](2.0, 1.5, 0.5, 1.6, 0.2, 2.4)
      val matrix: Matrix = Matrices.dense(3, 2, input)
      val vector: Vector = Vectors.dense(Array[Double](1.1, 0.5))
      val result1: DenseVector = matrix.multiply(vector)
      assert(result1.size == 3)
      println(result1)
      val result2: DenseMatrix = matrix.multiply(matrix.transpose.asInstanceOf[DenseMatrix])
      println(result2)

    }
}
