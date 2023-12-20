package org.pipeline.streams.spark.weatherTracking

import org.scalatest.flatspec.AnyFlatSpec
import org.pipeline.streams.spark.weatherTracking.WeatherTracking.modelPredictionSimulation

private[weatherTracking] final class WeatherTrackingTest extends AnyFlatSpec{

  it should "Succeed consuming weather data" in {
    import org.pipeline.streams.spark.implicits._

    val weatherTracking = new WeatherTracking(
      Seq[String]("weather", "doppler"),
      Seq[String]("monitor", "alert"),
      modelPredictionSimulation
    )
    weatherTracking.execute()
  }

}
