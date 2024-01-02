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
package org.pipeline.ga

import scala.util.Random

/**
 * Generic quantizer that convert an object of type T to and from a sequence of bits {0, 1}
 * implemented as a list
 * @tparam T Build-in type of feature (Int, Float,....)
 * @author Patrick Nicolas
 */
trait Quantizer[T]{
  val encodingLength: Int
  val range: Seq[T]

  /**
   * Generate a random value < maxValue
   * @return Random value of type T
   */
  def rand: T
  /**
   * Convert an object of type T into a bits sequence
   * @param t Object to be converted
   * @return Bits sequence
   */
  def apply(t: T): BitsRepr

  def unapply(bitsRepr: BitsRepr): T
}

/**
 * Quantizer for Boolean value (0, 1}
 * @param encodingLength Number of bits representing the value
 * @author Patrick Nicolas
 */
final class QuantizerBool(
  override val encodingLength: Int,
  override val range: Seq[Boolean] = Seq[Boolean](false, true)
) extends Quantizer[Boolean]{
  import scala.util.Random
  private[this] val encoder = new BitsIntEncoder(encodingLength)

  override def rand: Boolean = Random.nextBoolean()

  /**
   * Convert a boolean into a bits sequence
   * @param t Object to be converted
   * @return Bits sequence
   */
  override def apply(t: Boolean): BitsRepr = {
    val n: Int = if (t) 1 else 0
    encoder(n)
  }

  override def unapply(bitsRepr: BitsRepr): Boolean = encoder.unapply(bitsRepr) > 0
}


/**
 * Quantizer for integers. Only the integers which are less or equal to maximum authorized value
 * are generated.
 * @param encodingLength Number of bits representing the integer
 * @param range Range of valid values
 * @author Patrick Nicolas
 */
final class QuantizerInt(
  override val encodingLength: Int,
  override val range: Seq[Int]) extends Quantizer[Int]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)

  override def rand: Int = Random.shuffle(range).head

  /**
   * Convert an integer into a bits sequence
   *
   * @param t Integer to be converted
   * @throws GAException if the integer input is out of range
   * @return Bits sequence
   */
  @throws(classOf[GAException])
  override def apply(t: Int): BitsRepr = {
    if(!range.contains(t))
      throw new GAException(s"Value $t violates constraint of quantizer")
    encoder(t)
  }

  override def unapply(bitsRepr: BitsRepr): Int = encoder.unapply(bitsRepr)
}


/**
 * Quantizer for floating point value
 * @param encodingLength Number of bits representing the floating point value
 * @param range Range of valid values
 * @param scaleFactor Scaling factor applied to value prior to conversion
 * @author Patrick Nicolas
 */
class QuantizerFloat(
  override val encodingLength: Int,
  scaleFactor: Float,
  override val range: Seq[Float]) extends Quantizer[Float]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)

  override def rand: Float =  Random.shuffle(range).head

  /**
   * Convert a floating point value into a bits sequence
   *
   * @param x floating point value to be converted
   * @throws GAException if the floating point input is out of range
   * @return Bits sequence
   */
  @throws(classOf[GAException])
  override def apply(x: Float): BitsRepr = {
    if(!range.contains(x))
      throw new GAException(s"Value $x violates constraint of quantizer")
    encoder((scaleFactor*x).toInt)
  }

  override def unapply(bitsRepr: BitsRepr): Float = encoder.unapply(bitsRepr)/scaleFactor
}


