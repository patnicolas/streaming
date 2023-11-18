/**
 * Copyright 2022,2023 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.streaming.spark.weatherTracking

import org.apache.spark.sql.{Dataset, SparkSession}
import org.streaming.spark.weatherTracking.DopplerData.DopplerDataEncoder
import org.streaming.spark.weatherTracking.WeatherData.WeatherDataEncoder
import WeatherTracking._

/**
 *
 * @param inputTopics List of input topics
 * @param outputTopics List of output topics
 * @param sparkSession Implicit reference to the current Spark context
 *
 * @author Patrick Nicolas
 */
private[weatherTracking] final class WeatherTracking(
  inputTopics: Seq[String],
  outputTopics: Seq[String])(implicit sparkSession: SparkSession) {

  private def source: Option[(Dataset[WeatherData], Dataset[DopplerData])] = try {
    import sparkSession.implicits._

    val df = sparkSession
      .read
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", inputTopics.mkString(","))
      .load()
    val ds = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").as[(String, String)]

    val weatherDataDS = ds
      .filter(_._1.head == 'W')
      .map{ case (_, value) => WeatherDataEncoder.unapply(value) }
    val dopplerDataDS = ds.filter(_._1.head == 'D')
      .map {
        case (_, value) =>
          DopplerDataEncoder.unapply(value)
      }
    Some((weatherDataDS, dopplerDataDS))

  } catch {
    case e: Exception =>
      println(e.getMessage)
      None
  }

  private def consolidate(
    weatherDS: Dataset[WeatherData],
    dopplerDS: Dataset[DopplerData]): Dataset[ModelInputData] = {
    import sparkSession.implicits._
    import org.streaming.spark._


    val timedBucketedWeatherDS: Dataset[WeatherData] = weatherDS.map(
      wData => {
        val key = bucketKey(wData.timeStamp)
        wData.copy(timeStamp = key)
      })

    val timedBucketedDopplerDS = dopplerDS.map(
      wData => {
        val key = bucketKey(wData.timeStamp)
        wData.copy(timeStamp = key)
      })

    // Performed a fast, presorted join
    sortingJoin[WeatherData, DopplerData](
      timedBucketedWeatherDS,
      tDSKey = "timeStamp",
      timedBucketedDopplerDS,
      uDSKey = "timeStamp"
    ).map {
      case (weatherData, dopplerData) =>
        ModelInputData(
          weatherData.timeStamp,
          weatherData.temperature,
          weatherData.pressure,
          weatherData.humidity,
          dopplerData.windShear,
          dopplerData.windSpeed,
          dopplerData.gustSpeed,
          dopplerData.windDirection
        )
    }
  }

  private def sink(df: Dataset[(String, String)]): Option[Dataset[(String, String)]] = try {
    val ds = df
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", outputTopics.mkString(",")
    ).start()
    Some(df)
  } catch {
    case e: Exception => println(e.getMessage)
      None
  }



  def execute(): Unit = {
    source.foreach {
      case (weatherDS, dopplerDS) => {
        import sparkSession.implicits._
        println("Weather data:------\n")
        weatherDS.show()

        println("Doppler data:------\n")
        dopplerDS.show()
        consolidate(weatherDS, dopplerDS)

        sink(sparkSession.emptyDataset[(String, String)])
      }
    }
  }
}


object WeatherTracking {
  case class ModelInputData(
    timeStamp: String,
    temperature: Float,
    pressure: Float,
    humidity: Float,
    windShear: Boolean,
    windSpeed: Float,
    gustSpeed: Float,
    windDirection: Float
  )

  private def bucketKey(timeStamp: String): String = (timeStamp.toLong * 0.001).toLong.toString
}
