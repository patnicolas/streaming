package org.pipeline.kalman

import org.scalatest.flatspec.AnyFlatSpec

private[kalman] final class KalmanNoiseTest extends AnyFlatSpec {

  it should "Succeed generating 3 dimension noise for Kalman filter" in {
    val length = 3
    val processNoiseMean = 0.6
    val measurementNoiseMean = 0.8

    val kalmanNoise = KalmanNoise(processNoiseMean, measurementNoiseMean, length)
    println(s"Process noise:\n${kalmanNoise.processNoise}")
    println(s"Measurement noise:\n${kalmanNoise.processNoise}")
  }

  it should "Succeed generating 5 dimension normalized Kalman noise" in {
    val length = 5
    val kalmanNoise = KalmanNoise(length)
    println(s"Normalized process noise:\n${kalmanNoise.processNoise}")
    println(s"Normalized measurement noise:\n${kalmanNoise.processNoise}")
  }
}
