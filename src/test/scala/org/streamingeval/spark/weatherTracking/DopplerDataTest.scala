package org.streamingeval.spark.weatherTracking

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.spark.weatherTracking.DopplerData.DopplerDataEncoder
import org.streamingeval.spark.weatherTracking.WeatherData.WeatherDataEncoder

final class DopplerDataTest extends AnyFlatSpec {

  it should "Succeed generating data" in {
    val dopplerRadarLocations = Seq[(String, Float, Float)](
      ("station1", -120.544109F, 37.017784F),
      ("station2", -120.299812F, 37.170933F)
    )

    val numSamplesPerStations = 6
    val scaleFactor = 0.15F
    val dopplerRecords = DopplerData(
      dopplerRadarLocations,
      numSamplesPerStations,
      scaleFactor
    )
    assert(dopplerRecords.length == 12)
    val dopplerRecordsStr = dopplerRecords.map(_.toString).mkString("\n")
    println(dopplerRecordsStr)
  }


  it should "Succeed encoding Doppler radar data" in {
    val dopplerDataPoint = DopplerData(
      "my-station",
      -3123.0F,
      31.9F,
      System.currentTimeMillis(),
      windShear = false,
      10.0F,
      25.0F,
      50
    )
    // Encoder
    val encodedDopplerData = DopplerDataEncoder(dopplerDataPoint)
    // Decoder
    val decodedDopplerData = DopplerDataEncoder.unapply(encodedDopplerData)
    assert(dopplerDataPoint == decodedDopplerData)
  }
}
