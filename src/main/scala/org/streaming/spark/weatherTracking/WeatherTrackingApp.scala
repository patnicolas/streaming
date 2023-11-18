package org.streaming.spark.weatherTracking

object WeatherTrackingApp extends App {
  import org.streaming.spark.implicits._

  val weatherTracking = new WeatherTracking(
    Seq[String]("weather", "doppler"),
    Seq[String]("monitor", "alert")
  )
  weatherTracking.execute()
  Thread.sleep(5000L)
}
