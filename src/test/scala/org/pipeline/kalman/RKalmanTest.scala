package org.pipeline.kalman

import org.scalatest.flatspec.AnyFlatSpec
import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrices, Matrix, Vector, Vectors}

private[kalman] final class RKalmanTest extends AnyFlatSpec{

  ignore should "succeed operating on dense vectors and matrices" in {
      val input: Array[Double] = Array[Double](2.0, 1.5, 0.5, 1.6, 0.2, 2.4)
      val matrix: Matrix = Matrices.dense(3, 2, input)
      val vector: Vector = Vectors.dense(Array[Double](1.1, 0.5))
      val result1: DenseVector = matrix.multiply(vector)
      assert(result1.size == 3)
      println(result1)
      val result2: DenseMatrix = matrix.multiply(matrix.transpose.asInstanceOf[DenseMatrix])
      println(result2)
  }

  ignore should "Succeed predicting next state" in {
    import RKalmanTest._

    val x = Array[Double](3.0, 1.0, 1.0)
    println(s"First measurement:\n${x.mkString(", ")}")
    val recursiveKalman = new RKalman(kalman3Parameters(x))
    val nextMeasurement = recursiveKalman.predict()
    println(s"Second measurement:\n${recursiveKalman.getState}")
  }

  ignore should "Succeed update next state and compute gain for a two dimension state" in {
    import RKalmanTest._
    val x = Array[Double](0.0, 12.0)
    println(s"Initial measurement:\n${x.mkString(", ")}")
    val recursiveKalman = new RKalman(kalman2Parameters(x))

    val z = Array[Double](0.1, 10.0)
    val kGain = recursiveKalman.update(z)
    println(s"Kalman gain:\n ${kGain.map(_.mkString(" ")).mkString("\n")}")
  }


  it should "Succeed recursive application of Kalman for a two dimension state" in {
    import RKalmanTest._
    val xInitial = Array[Double](0.0, 0.0)
    val recursiveKalman = new RKalman(kalman2Parameters(xInitial))

    val measurements = Array.tabulate(20)(n => Array[Double](n*n*0.01 + 0.002/(n +2), 0.0))
    println(s"Measurements:\n${measurements.map(_.head).mkString(", ")}")
    val states = recursiveKalman(measurements).map(_.head)
    println(s"States:\n ${states.mkString(", ")}")
  }

  ignore should "Succeed update next state and compute gain for a three dimension state" in {
    import RKalmanTest._

    val x = Array[Double](3.0, 1.0, 1.0)
    println(s"First measurement:\n${x.mkString(", ")}")
    val recursiveKalman = new RKalman(kalman3Parameters(x))

    val z = Array[Double](3.1, 1.0, 0.95)
    val kGain = recursiveKalman.update(z)
    println(s"Kalman gain:\n ${kGain.map(_.mkString(" ")).mkString("\n")}")
  }


  it should "Succeed processing and display Kalman prediction" in {
    val z = Array.tabulate(20)(n => n*n*0.01 + 0.002/(n +2) + normalRandomValue(0.2))

  }

}


private[kalman] object RKalmanTest {
  implicit val gaussKalmanNoise: KalmanNoise = KalmanNoise(0.5, 0.6, 2)

  private def kalman3Parameters(x: Array[Double]): KalmanParameters = {
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


  private def kalman2Parameters(x: Array[Double]): KalmanParameters = {
    val velocity = 0.0167
    val A = Array[Array[Double]](
      Array[Double](1.0, velocity),
      Array[Double](0.0, 1.0)
    )
    val H = Array[Array[Double]](
      Array[Double](1.0, 0.0),
      Array[Double](0.0, 0.0)
    )
    val P = Array[Array[Double]](
      Array[Double](4.0, 0),
      Array[Double](0.0, 4.0)
    )
    KalmanParameters(A, None, H, Some(P), x)
  }
}
