package org.streamingeval.spark.weatherTracking

import org.scalatest.flatspec.AnyFlatSpec

final class TrackingDataGeneratorTest extends AnyFlatSpec  {

  it should "Succeed generating and producing weather to Kafka" in {
    val weatherStations = Seq[(String, Float, Float)](
      ("station1", -122.207708F, 37.426888F),
      ("station2", -121.987203F, 37.450981F)
    )
    val numSamplesPerStations = 4
    val scaleFactor = 0.2F

    TrackingDataGenerator(
      weatherStations,
      Seq.empty[(String, Float, Float)],
      numSamplesPerStations,
      scaleFactor)
  }


  it should "Succeed generating and producing weather to Kafka" in {
    val dopplerRadars = Seq[(String, Float, Float)](
      ("station1", -122.207708F, 37.426888F),
      ("station2", -121.987203F, 37.450981F)
    )
    val numSamplesPerStations = 4
    val scaleFactor = 0.2F

    TrackingDataGenerator(
      Seq.empty[(String, Float, Float)],
      dopplerRadars,
      numSamplesPerStations,
      scaleFactor
    )
  }
}
