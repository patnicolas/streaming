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
package org.streaming.spark

package object weatherTracking{

  trait TrackingData[T <: TrackingData[T]] {
    self =>
    val id: String           // Identifier for the weather station
    val longitude: Float     // Longitude for the weather station
    val latitude: Float      // Latitude for the weather station
    val timeStamp: String    // Time stamp data is collected
  }


  class DataEncoder[T <: TrackingData[T]] {
    def apply(t: T): String = t.toString
    def unapply(encodedData: String, ctr: Seq[String] => T): T = {
      val fields = encodedData.split(";")
      ctr(fields)
    }
  }
}
