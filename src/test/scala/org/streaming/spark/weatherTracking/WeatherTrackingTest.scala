package org.streaming.spark.weatherTracking

import org.scalatest.flatspec.AnyFlatSpec
import org.streaming.spark.weatherTracking.WeatherTracking.modelPredictionSimulation

class WeatherTrackingTest extends AnyFlatSpec{

  it should "Succeed consuming weather data" in {
    import org.streaming.spark.implicits._

    val weatherTracking = new WeatherTracking(
      Seq[String]("weather", "doppler"),
      Seq[String]("monitor", "alert"),
      modelPredictionSimulation
    )
    weatherTracking.execute()
  }

}
