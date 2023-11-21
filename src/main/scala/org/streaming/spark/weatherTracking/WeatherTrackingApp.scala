package org.streaming.spark.weatherTracking

import org.apache.spark.sql.Dataset
import org.streaming.spark.weatherTracking.ModelData.{CellArea, ModelInputData, WeatherAlert}
import org.streaming.spark.weatherTracking.WeatherTracking.modelPredictionSimulation



object WeatherTrackingApp extends App {
  import org.streaming.spark.implicits._


  val weatherTracking = new WeatherTracking(
    Seq[String]("weather", "doppler"),
    Seq[String]("monitor", "alert"),
    modelPredictionSimulation
  )
  weatherTracking.execute()
  Thread.sleep(5000L)
}
