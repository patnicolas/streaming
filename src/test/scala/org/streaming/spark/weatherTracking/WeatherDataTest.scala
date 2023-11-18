package org.streaming.spark.weatherTracking

import org.scalatest.flatspec.AnyFlatSpec
import org.streaming.spark.weatherTracking.WeatherData.WeatherDataEncoder

import scala.util.Random

final class WeatherDataTest extends AnyFlatSpec {

  it should "Succeed generating data" in {
    val weatherStations = Seq[(String, Float, Float)](
      ("station1", -122.207708F, 37.426888F),
      ("station2", -121.987203F, 37.450981F)
    )

    val numSamplesPerStations = 4
    val scaleFactor = 0.2F
    val weatherRecords = WeatherData(weatherStations, numSamplesPerStations, scaleFactor)
    assert(weatherRecords.length == 8)
    val weatherRecordsStr = weatherRecords.map( _.toString).mkString("\n")
    println(weatherRecordsStr)
  }

  it should "Succeed encoding weather data" in {
    val weatherDataPoint = WeatherData(
      "my-station",
      -3123.0F,
      31.9F,
      System.currentTimeMillis().toString,
      60.0F,
      1089.0F,
      50.0F)
    // Encoder
    val encodedWeatherData = WeatherDataEncoder(weatherDataPoint)
    // Decoder
    val decodedWeatherData = WeatherDataEncoder.unapply(encodedWeatherData)
    assert(weatherDataPoint == decodedWeatherData)
  }
}
