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
 * Interface to the various implementation Bloom Filters
 * The 3 key methods are
 * - add (Add an element to the filter)
 * - addAll (add multiple elements to the filter)
 * - mightContain (Test if an element might be contained in the filter)
 *
 * @tparam T Type of elements to be added to the filter
 *
 * @author Patrick Nicolas
 */
trait TBloomFilter[T]{
  def add(t: T): Unit

  def addAll(ts: Array[T]): Unit
  def mightContain(t: T): Boolean

}
