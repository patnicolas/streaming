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

import scala.util.Random

/**
 * Definition of weather data generated by a local weather station.
 *
 * @param id Identifier for the source of Weather data
 * @param longitude Longitude of the source of Weather data
 * @param latitude Latitude of the source of Weather data
 * @param timeStamp Time stamp for the new weather data
 * @param temperature Temperature in Fahrenheit
 * @param pressure Barometric pressure
 * @param humidity Humidity percentage
 * @throws IllegalArgumentException if temperature, pressure or humidity are out-of-bounds
 *
 * @author Patrick Nicolas
 */
@throws(clazz = classOf[IllegalArgumentException])
private[weatherTracking] case class WeatherData (
  override val id: String,                // Identifier for the weather station
  override val longitude: Float,          // Longitude for the weather station
  override val latitude: Float,           // Latitude for the weather station
  override val timeStamp: String = System.currentTimeMillis().toString,      // Time stamp data is
  // collected
  temperature: Float = 56.0F,             // Temperature (Fahrenheit) collected at timeStamp
  pressure: Float = 1022.0F,              // Pressure (millibars) collected at timeStamp
  humidity: Float = 25.0F) extends TrackingData  {  // Humidity (%) collected at timeStamp

  require(temperature > -20.0F && temperature < 120.0F,
    s"Temperature $temperature should be [20, 120] F")
  require(pressure > 900.0F && pressure < 1250.0F,
    s"pressure $pressure should be [900, 1250] millibars")
  require(humidity >= 0.0F && humidity <= 100.0F, s"Humidity $humidity should be [0, 100] %")

  def apply(rand: Random, scaleFactor: Float): WeatherData =
    this.copy(
      timeStamp = (timeStamp.toLong + 10000L + rand.nextInt(2000)).toString,
      temperature = temperature*(1 + scaleFactor*rand.nextFloat()),
      pressure = pressure*(1 + scaleFactor*rand.nextFloat()),
      humidity = humidity*(1 + scaleFactor*rand.nextFloat())
    )

  override def toString: String =
    s"$id;$longitude;$latitude;$timeStamp;$temperature;$pressure;$humidity"
}



private[weatherTracking] object WeatherData {

  /**
   * Encoder (apply) - Decoder (Unapply) for WeatherData for Kafka channel
   */
  object WeatherDataEncoder extends DataEncoder[WeatherData] {
    def unapply(encodedWeatherData: String): WeatherData =
      super.unapply(
        encodedWeatherData,
        (fields: Seq[String]) => WeatherData(
          fields.head,
          fields(1).toFloat,
          fields(2).toFloat,
          fields(3),
          fields(4).toFloat,
          fields(5).toFloat,
          fields(6).toFloat
        )
      )
  }


  def apply(filename: String, loader: String => Seq[WeatherData]): Seq[WeatherData] =
    loader(filename)


  def apply(
    initialStations: Seq[(String, Float, Float)],
    numSamplesPerStation: Int,
    scaleFactor: Float): Seq[WeatherData] = {

    val seedStations = initialStations.map{ case (id, long, lat) => WeatherData(id, long, lat) }
    val rand = new Random(42L)
    seedStations.flatMap(
      seedStation => (0 until numSamplesPerStation).map(_ => seedStation(rand, scaleFactor))
    )
  }
}