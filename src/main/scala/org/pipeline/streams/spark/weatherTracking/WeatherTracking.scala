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
package org.pipeline.streams.spark.weatherTracking

import org.apache.spark.sql.{Dataset, SparkSession}
import org.pipeline.streams.spark.weatherTracking.DopplerData.DopplerDataEncoder
import org.pipeline.streams.spark.weatherTracking.WeatherData.WeatherDataEncoder
import org.pipeline.streams.spark.weatherTracking.ModelData.{CellArea, ModelInputData, StormPrediction}
import WeatherTracking._
import org.slf4j.{Logger, LoggerFactory}
import org.pipeline.streams.spark.implicits.sparkSession

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
  model: Dataset[ModelInputData] => Seq[StormPrediction])(implicit sparkSession: SparkSession) {
  require(inputTopics.nonEmpty && outputTopics.nonEmpty, "Input or output topics are undefined")

  /**
   * Step 1: Load data from sources (Weather station and Doppler radar) <- Source
   * Step 2: Consolidate the data from sources (time stamp)
   * Step 3: Predict the storm or tornado by generating a weather alert
   * Step 4: Produce alerts and monitoring data to Kafka channels >- Sink
   */
  def execute(): Unit = {
    // Load data from sources (Weather station and Doppler radar)'
    for {
      (weatherDS, dopplerDS) <- source                                 // Step 1
      consolidatedDataset <- synchronizeSources(weatherDS, dopplerDS)  // Step 2
      predictions <- predict(consolidatedDataset)                      // Step 3
      consolidatedDataset <- sink(predictions)                         // Step 3
    } yield {
      consolidatedDataset
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
    val dopplerDataDS = ds
      .filter(_._1.head == 'D')
      .map { case (_, value) => DopplerDataEncoder.unapply(value) }
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
    import org.pipeline.streams.spark._

    val timedBucketedWeatherDS = weatherDS.map(
      wData =>wData.copy(timeStamp = bucketKey(wData.timeStamp))
    )
    val timedBucketedDopplerDS = dopplerDS.map(
      wData => wData.copy(timeStamp = bucketKey(wData.timeStamp))
    )

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

  private def predict(modelInput: Dataset[ModelInputData]): Option[Dataset[StormPrediction]] = try {
    import sparkSession.implicits._
    Some(model(modelInput).toDS())
  } catch {
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }

  private def sink(stormAlerts: Dataset[StormPrediction]): Option[Dataset[String]] =
    try {
      import sparkSession.implicits._

      // Encoded dataset of weather alerts to be produced to Kafka topic
      val encStormAlerts = stormAlerts.map(_.toString)
      encStormAlerts
        .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
        .writeStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", outputTopics.mkString(",")
      ).start()
      Some(encStormAlerts)
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
        outlier => StormPrediction(
          "alert",
          intensity,
          probability,
          outlier.timeStamp,
          modelInputData,
          cellArea
        )
      ).collect().toSeq
    } else Seq.empty[StormPrediction]
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
