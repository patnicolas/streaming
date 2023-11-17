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
package org.streamingeval.spark.weatherTracking

import org.apache.kafka.common.KafkaException
import org.streamingeval.kafka.KafkaAdminClient.consumerProperties
import org.streamingeval.kafka.prodcons.TypedKafkaProducer


/**
 *
 * @param topic Topic to which produce Kafka data
 * @author Patrick Nicolas
 */
private[weatherTracking] final class TrackingDataGenerator(topic: String) extends
  TypedKafkaProducer[String](consumerProperties, topic)


/**
 * Singleton for generating and producing Weather and Doppler Radar to Kafka.
 */
private[weatherTracking] object TrackingDataGenerator {
  private val weatherDataTopic = "weather"
  private val dopplerDataTopic = "doppler"

  private val weatherDataGenerator = new TrackingDataGenerator(weatherDataTopic)
  private val dopplerDataGenerator = new TrackingDataGenerator(dopplerDataTopic)

  /**
   * Generate the list of weather and doppler data using random generator and produced
   * to the current Kafka Connect server.
   *
   * @param weatherDataStations Weather data collection stations
   * @param dopplerDataStations Doppler radar data collection stations
   * @param numSamplesPerStations Number of random samples per stations
   * @param scaleFactor Scale factor for random update of new sample
   * @return Tuple (weather data records, doppler radar data records)
   */
  def apply(
    weatherDataStations: Seq[(String, Float, Float)],
    dopplerDataStations: Seq[(String, Float, Float)],
    numSamplesPerStations: Int,
    scaleFactor: Float
  ): Unit = try {
    // Generate the weather and Doppler radar records
    val weatherRecords = WeatherData(weatherDataStations, numSamplesPerStations, scaleFactor)
    val dopplerRecords = DopplerData(dopplerDataStations, numSamplesPerStations, scaleFactor)
    // Send the weather and Doppler radar records to Kafka
    weatherDataGenerator.send(weatherRecords.map(wr => (wr.id, wr.toString)))
    dopplerDataGenerator.send(dopplerRecords.map(dr => (dr.id, dr.toString)))
  } catch {
    case e: KafkaException => println(e.getMessage)
    case e: Exception => println(e.getMessage)
  }
}