package org.pipeline.kalman

import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector}
import org.scalatest.flatspec.AnyFlatSpec

private[kalman] final class KalmanParametersTest extends AnyFlatSpec{

  it should "Succeed generating identity matrix for dimension 6" in {
    val m6 = identityMatrix(6)
    println(m6.map(_.mkString(" ")).mkString("\n"))
  }

  it should "Succeed instantiating the parameters of the Kalman filter" in {
    val velocity = 0.0167
    val A = Array[Array[Double]](
      Array[Double](1.0, velocity, 0.0),
      Array[Double](0.0, 1.0,  velocity),
      Array[Double](0.0, 0.0, 1.0)
    )
    val H = identityMatrix(3)
    val x = Array[Double](3.0, 1.0, 0.0)

    val kalmanParameters = KalmanParameters(A, H, x)
    println(s"A transposed:\n${kalmanParameters.ATranspose}")

    val z = Array[Double](0.3, 1.3, 1.0)
    val diff = kalmanParameters.measureDiff(new DenseVector(z))
    println(diff)
  }


  it should "Succeed instantiating the parameters of the Kalman filter with easurements"in {
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
    val x = Array[Double](3.0, 1.0, 0.0)

    val kalmanParameters = KalmanParameters(A, H, x)

    val z = Array[Double](0.3, 1.3, 1.0)
    val diff = kalmanParameters.measureDiff(new DenseVector(z))
    println(s"Diff measurement: {diff}")
    val S = kalmanParameters.computeS(new DenseMatrix(
      3,
      3,
      randUniformMatrix(z.length).flatten))
    println(s"S:\n$S")

    val kalmanGain = kalmanParameters.gain(S)
    println(s"Kalman Gain:\n${kalmanGain}")
  }
}
