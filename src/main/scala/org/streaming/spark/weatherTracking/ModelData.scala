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


/**
 * Object to wraps model input and output data classes
 * @author Patrick Nicolas
 */
private[weatherTracking] object ModelData{

  /**
   * Data class for input to model
   * @param timeStamp Time stamp for the new consolidated data, input to model
   * @param temperature Temperature in Fahrenheit
   * @param pressure Barometric pressure in millibars
   * @param humidity Humidity in percentage
   * @param windShear Boolean flag to specify if this is a wind shear
   * @param windSpeed Average speed for the wind (miles/hour)
   * @param gustSpeed Maximum speed for the wind (miles/hour)
   * @param windDirection Direction of the wind [0, 360] degrees
   */
  case class ModelInputData(
    timeStamp: String,   // Time stamp for the new consolidated data, input to model
    temperature: Float,  // Temperature in Fahrenheit
    pressure: Float,     // Barometric pressure in millibars
    humidity: Float,     // Humidity in percentage
    windShear: Boolean,  // Boolean flag to specify if this is a wind shear
    windSpeed: Float,    // Average speed for the wind (miles/hour)
    gustSpeed: Float,    // Maximum speed for the wind (miles/hour)
    windDirection: Float // Direction of the wind [0, 360] degrees
  )


  /**
   * Data class that defines the area covered by an alert of serious stormy weather
   *
   * @param polygon List of pairs of longitude and latitude that defines the area of serious
   *                storms or tornadoes */
  case class CellArea(polygon: Seq[(Float, Float)] = Seq.empty[(Float, Float)]){
    require(
      polygon.isEmpty || polygon.length > 2,
      s"Cell area should have at least 3 vertices"
    )

    override def toString: String = if (polygon.nonEmpty) polygon.map { case (long, lat) => s"$long,$lat" }.mkString(";") else ""
  }


  /**
   * Data class for Storm prediction or alert
   *
   * @param id             Identifier of the alert/message
   * @param intensity      Intensity of storm or Tornado [1, to 5
   * @param probability    Probability of a tornado of a given intensity develop
   * @param timeStamp      Time stamp of the alert
   * @param modelInputData Weather and Doppler radar data used to generate/predict the alert
   * @param cellArea       Area covered by the alert
   */
  case class StormPrediction(
    id: String,                    // Identifier of the alert/message
    intensity: Int,                // Intensity of storm or Tornado [1, to 5]
    probability: Float,            // Probability of a storm of a given intensity develop
    timeStamp: String,             // Time stamp of the alert or prediction
    modelInputData: ModelInputData,// Weather and Doppler radar data used to generate/predict the alert
    cellArea: CellArea             // Area covered by the alert/prediction
  ){
    override def toString: String =
      s"$id;$intensity;$probability;$timeStamp;${modelInputData.toString};${cellArea.toString}"
  }

  object StormPrediction{
    def apply(
      id: String,
      modelInputData: ModelInputData
    ): StormPrediction = StormPrediction(
      id,
      0,
      0.0F,
      System.currentTimeMillis().toString,
      modelInputData,
      CellArea()
    )
  }
}
