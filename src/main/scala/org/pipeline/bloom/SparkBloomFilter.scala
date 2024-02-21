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
package org.pipeline.bloom

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.util.sketch.BloomFilter


/**
 *
 * @param capacity
 * @param targetFPRate
 * @tparam T
 */

private[bloom] final class SparkBloomFilter[T] private (bloomFilter: BloomFilter)
  extends TBloomFilter[T] {

  def getExpectedFPRate: Double = bloomFilter.expectedFpp()
  override def mightContain(t: T): Boolean = bloomFilter.mightContain(t)

  override def add(t: T): Unit = bloomFilter.put(t)

  override def addAll(ts: Array[T]): Unit =
    if(ts.nonEmpty)
      ts.foreach(add)
}

private[bloom] object SparkBloomFilter {

  /**
   *
   * @param capacity
   * @param targetFPRate
   * @tparam T
   * @return
   */
  def apply[T](capacity: Int, targetFPRate: Float): SparkBloomFilter[T] =
    new SparkBloomFilter[T]( BloomFilter.create(capacity, targetFPRate))

  /**
   *
   * @param data
   * @param columnName
   * @param capacity
   * @param targetFPRate
   * @param sparkSession
   * @tparam T
   * @return
   */
  def apply[T](
    data: Dataset[T],
    columnName: String,
    capacity: Int,
    targetFPRate: Double)(implicit sparkSession: SparkSession): SparkBloomFilter[T]= {
    val filter = data.stat.bloomFilter(columnName, capacity, targetFPRate)
    new SparkBloomFilter[T](filter)
  }
}
