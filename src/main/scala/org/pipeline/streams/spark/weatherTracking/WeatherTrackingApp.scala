/**
 * Copyright 2022,2024 Patrick R. Nicolas. All Rights Reserved.
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

import org.pipeline.streams.spark.weatherTracking.WeatherTracking.modelPredictionSimulation


/**
 * Main application for tracking weather and predicting storms
 */
object WeatherTrackingApp extends App {
  import org.pipeline.streams.spark.implicits._

  private val weatherTracking = new WeatherTracking(
    Seq[String]("weather", "doppler"),
    Seq[String]("monitor", "alert"),
    modelPredictionSimulation
  )
  weatherTracking.execute()
  Thread.sleep(5000L)
}
