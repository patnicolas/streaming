package org.pipeline.kalman

import org.scalatest.flatspec.AnyFlatSpec
import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrices, Matrix, Vector, Vectors}

private[kalman] final class RKalmanTest extends AnyFlatSpec{

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

  it should "Succeed predicting next state" in {
    import RKalmanTest._

    val x = Array[Double](3.0, 1.0, 1.0)
    println(s"First measurement:\n${x.mkString(", ")}")
    val recursiveKalman = new RKalman(kalmanParameters(x))
    val nextMeasurement = recursiveKalman.predict()
    println(s"Second measurement:\n${recursiveKalman.getState}")
  }

  it should "Succeed update next state and compute gain" in {
    import RKalmanTest._

    val x = Array[Double](3.0, 1.0, 1.0)
    println(s"First measurement:\n${x.mkString(", ")}")
    val recursiveKalman = new RKalman(kalmanParameters(x))

    val z = Array[Double](3.1, 1.0, 0.95)
    val kGain = recursiveKalman.update(z)
    println(s"Kalman gain:\n ${kGain.map(_.mkString(" ")).mkString("\n")}")
  }

}


private[kalman] object RKalmanTest {
  implicit val gaussKalmanNoise: KalmanNoise = KalmanNoise(0.5, 0.6, 3)

  private def kalmanParameters(x: Array[Double]): KalmanParameters = {
    val velocity = 0.0167
    val A = Array[Array[Double]](
      Array[Double](1.0, velocity, 0.0),
      Array[Double](0.0, 1.0, velocity),
      Array[Double](0.0, 0.0, 1.0)
    )
    val H = Array[Array[Double]](
      Array[Double](1.0, 0.02, 0.0),
      Array[Double](0.0, 0.9, 0.5),
      Array[Double](0.0, 0.6, 1.0)
    )
    val P = Array[Array[Double]](
      Array[Double](0.2, 0.02, 0.0),
      Array[Double](0.4, 0.9, 0.1),
      Array[Double](0.0, 0.6, 1.0)
    )
    KalmanParameters(A, None, H, Some(P), x)
  }
}
