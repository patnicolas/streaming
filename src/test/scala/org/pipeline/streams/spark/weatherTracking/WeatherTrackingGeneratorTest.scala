package org.pipeline.streams.spark.weatherTracking

import org.scalatest.flatspec.AnyFlatSpec

private[weatherTracking] final class WeatherTrackingGeneratorTest extends AnyFlatSpec  {

  ignore should "Succeed generating and producing weather to Kafka" in {
    val weatherStations = Seq[(String, Float, Float)](
      ("s1", -122.207708F, 37.426888F),
      ("s2", -121.987203F, 37.450981F)
    )
    val numSamplesPerStations = 4
    val scaleFactor = 0.2F

    WeatherTrackingGenerator(
      weatherStations,
      Seq.empty[(String, Float, Float)],
      numSamplesPerStations,
      scaleFactor)
  }


  ignore should "Succeed generating and producing Doppler data to Kafka" in {
    val dopplerRadars = Seq[(String, Float, Float)](
      ("s1", -122.207708F, 37.426888F),
      ("s2", -121.987203F, 37.450981F)
    )
    val numSamplesPerStations = 4
    val scaleFactor = 0.2F

    WeatherTrackingGenerator(
      Seq.empty[(String, Float, Float)],
      dopplerRadars,
      numSamplesPerStations,
      scaleFactor
    )
  }

  it should "Succeed generating Weather and Doppler radar data into Kafka" in {
    val weatherStation = ("s1", -122.207708F, 37.426888F)
    val dopplerRadar = ("s1", -122.226688F, 37.500912F)

    val numSamplesPerStations = 6
    val scaleFactor = 0.2F

    WeatherTrackingGenerator(
      weatherStation,
      dopplerRadar,
      numSamplesPerStations,
      scaleFactor,
      4000
    )
  }
}
