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

import scala.util.Random

/**
 * Definition of data generated by Doppler radar station
 *
 * @param id Identifier for the source of Doppler radar data
 * @param longitude Longitude of the source of Doppler radar data
 * @param latitude Latitude of the source of Doppler radar data
 * @param timeStamp Time stamp for the new Doppler radar data
 * @param windShear Boolean flag to specify if this is a wind shear
 * @param speed Average speed for the wind
 * @param gustSpeed Maximum speed for the wind
 * @param direction Direction of the wind
 *
 * @author Patrick Nicolas
 */
case class DopplerData(
  override val id: String, // Identifier for the weather station
  override val longitude: Float, // Longitude for the weather station
  override val latitude: Float, // Latitude for the weather station
  override val timeStamp: Long = System.currentTimeMillis(), // Time stamp data is collected
  windShear: Boolean = false,
  speed: Float = 0.0F,
  gustSpeed: Float = 0.0F,
  direction: Int = 0) extends TrackingData {

  def apply(
    rand: Random,
    scaleFactor: Float
  ): DopplerData = this.copy(
    timeStamp = timeStamp + 10000L + rand.nextInt(2000),
    windShear = rand.nextBoolean(),
    speed = speed * (1 + scaleFactor * rand.nextFloat()),
    gustSpeed = gustSpeed * (1 + scaleFactor * rand.nextFloat()),
    {
      val newDirection = direction * (1 + scaleFactor * rand.nextFloat())
      direction = if(newDirection > 360.0) newDirection.toInt%360 else ewDirection.toInt
    }
  )
}



object DopplerData {

  object DopplerDataEncoder extends DataEncoder[DopplerData] {
    def unapply(encodedDopplerData: String): DopplerData =
      super.unapply(
        encodedDopplerData,
        (fields: Seq[String]) => DopplerData(
          fields.head,
          fields(1).toFloat,
          fields(2).toFloat,
          fields(3).toLong,
          fields(4).toBoolean,
          fields(5).toFloat,
          fields(6).toFloat,
          fields(7).toInt
        )
      )
  }

  def apply(
    filename: String,
    loader: String => Seq[DopplerData]
  ): Seq[DopplerData] = loader(filename)

  def apply(
    initialStations: Seq[(String, Float, Float)],
    numSamplesPerStation: Int,
    scaleFactor: Float
  ): Seq[DopplerData] = {

    val seedStations = initialStations.map { case (id, long, lat) => DopplerData(id, long, lat)}
    val rand = new Random(42L)
    seedStations.flatMap(
      seedStation => (0 until numSamplesPerStation).map(
        _ => seedStation(
          rand,
          scaleFactor
        )
      )
    )
  }
}
