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
import org.streaming.spark.weatherTracking.ModelData.{CellArea, ModelInputData, WeatherAlert}
import WeatherTracking._
import org.slf4j.{Logger, LoggerFactory}
import org.streaming.spark.implicits.sparkSession

/**
 * Weather tracking data streaming pipeline.
 * @param inputTopics List of input topics
 * @param outputTopics List of output topics
 * @param model Trained model that predict/infer an alert given a sequence of weather and
 *              Doppler radar data synchronized by location (longitude and latitude) and time stamp.
 * @param sparkSession Implicit reference to the current Spark context
 *
 * @author Patrick Nicolas
 */
private[weatherTracking] final class WeatherTracking(
  inputTopics: Seq[String],
  outputTopics: Seq[String],
  model: Dataset[ModelInputData] => Seq[WeatherAlert])(implicit sparkSession: SparkSession) {
  require(inputTopics.nonEmpty && outputTopics.nonEmpty, "Input or output topics are undefined")

  /**
   * Step 1: Load data from sources (Weather station and Doppler radar)
   * Step 2: Consolidate the data from sources (time stamp)
   */
  def execute(): Unit = {
    // Load data from sources (Weather station and Doppler radar)'
    for {
      (weatherDS, dopplerDS) <- source
      consolidatedDataset <- synchronizeSources(weatherDS, dopplerDS)
      predictions <- predict(consolidatedDataset)
      consolidatedDataset <- sink(predictions)
    } yield {
      predictions
    }
  }

  // --------------------------   Helper methods ---------------------------------

  private def source: Option[(Dataset[WeatherData], Dataset[DopplerData])] = try {
    import sparkSession.implicits._

    val df = sparkSession
      .read
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", inputTopics.mkString(","))
      .load()
    val ds = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").as[(String, String)]

    // Convert weather data stream into objects
    val weatherDataDS = ds
      .filter(_._1.head == 'W')
      .map{ case (_, value) => WeatherDataEncoder.unapply(value) }

    // Convert Doppler radar data stream into objects
    val dopplerDataDS = ds.filter(_._1.head == 'D')
      .map {
        case (_, value) =>
          DopplerDataEncoder.unapply(value)
      }
    // For debugging only
    WeatherTracking.show(weatherDataDS, dopplerDataDS)
    Some((weatherDataDS, dopplerDataDS))
  } catch {
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }


  private def synchronizeSources(
    weatherDS: Dataset[WeatherData],
    dopplerDS: Dataset[DopplerData]): Option[Dataset[ModelInputData]] = try {
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
    val output = sortingJoin[WeatherData, DopplerData](
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
    Some(output)
  }
  catch {
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }

  private def predict(modelInput: Dataset[ModelInputData]): Option[Dataset[WeatherAlert]] = try {
    import sparkSession.implicits._
    Some(model(modelInput).toDS())
  } catch {
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }

  private def sink(weatherAlerts: Dataset[WeatherAlert]): Option[Dataset[String]] =
    try {
      import sparkSession.implicits._

      // Stringize dataset of weather alerts to be produced to Kafka topic
      val weatherAlertsStr = weatherAlerts.map( weatherAlert => weatherAlert.toString)
      weatherAlertsStr
        .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
        .writeStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", outputTopics.mkString(",")
      ).start()
      Some(weatherAlertsStr)
  } catch {
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }
}


object WeatherTracking {
  final val logger: Logger = LoggerFactory.getLogger("WeatherTracking")

  /**
   * Function to simulate the prediction of tornadoes given data provided by weather station
   * and Doppler radar.
   */
  final val modelPredictionSimulation = (inputWeatherDataDS: Dataset[ModelInputData]) => {
    val outliersDS = inputWeatherDataDS.filter(
      data => data.windShear && data.pressure < 965.0F && data.humidity > 65.0F && data.windSpeed > 25.0F
    )

    if (!outliersDS.isEmpty) {
      import sparkSession.implicits._

      val intensity = 50
      val probability = 0.95F
      val modelInputData = outliersDS.head()
      val cellArea = CellArea()
      outliersDS.map(
        outlier => WeatherAlert(
          "alert",
          intensity,
          probability,
          outlier.timeStamp,
          modelInputData,
          cellArea
        )
      ).collect().toSeq
    } else Seq.empty[WeatherAlert]
  }

  private def bucketKey(timeStamp: String): String = (timeStamp.toLong * 0.001).toLong.toString

  private def show(
    weatherDataDS: Dataset[WeatherData],
    dopplerDataDS: Dataset[DopplerData]
  ): Unit = {
    logger.info("Weather data:------\n")
    weatherDataDS.show()
    logger.info("Doppler data:------\n")
    dopplerDataDS.show()
  }
}
