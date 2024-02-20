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


/**
 *
 * @param capacity
 * @param falsePositiveRate
 * @tparam T
 */

private[bloom] final class SparkBloomFilter[T](
  capacity: Int,
  falsePositiveRate: Float) extends BloomFilter[T] {
  import org.apache.spark.util.sketch._

  private[this] val bloomFilter = BloomFilter.create(capacity, falsePositiveRate)

  override def mightContain(t: T): Boolean = bloomFilter.mightContain(t)

  override def add(t: T): Unit = bloomFilter.put(t)

  override def addAll(ts: Array[T]): Unit =
    if(ts.nonEmpty)
      ts.foreach(add)
}
