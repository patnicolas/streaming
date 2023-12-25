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

/**
 * Generic quantizer that convert an object of type T to and from a sequence of bits {0, 1}
 * implemented as a list
 * @tparam T Type of object
 * @author Patrick Nicolas
 */
trait Quantizer[T]{
  val encodingLength: Int

  /**
   * Convert an object of type T into a bits sequence
   * @param t Object to be converted
   * @return Bits sequence
   */
  def apply(t: T): BitsRepr

  def unapply(bitsRepr: BitsRepr): T

  def isValid(t: T): Boolean
}

/**
 * Quantizer for Boolean value (0, 1}
 * @param encodingLength Number of bits representing the value
 */

final class QuantizerBool(
  override val encodingLength: Int
) extends Quantizer[Boolean]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)

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

  override def isValid(b: Boolean): Boolean = true
}


/**
 * Quantizer for integers
 * @param encodingLength Number of bits representing the integer
 * @param constraint Constraint on the integer prior conversion
 */

final class QuantizerInt(
  override val encodingLength: Int,
  constraint: Int => Boolean) extends Quantizer[Int]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)

  /**
   * Convert an integer into a bits sequence
   *
   * @param t Integer to be converted
   * @throws GAException if the integer input is out of range
   * @return Bits sequence
   */
  @throws(classOf[GAException])
  override def apply(t: Int): BitsRepr = {
    if(!constraint(t))
      throw new GAException(s"Value $t violates constraint of quantizer")
    encoder(t)
  }

  override def unapply(bitsRepr: BitsRepr): Int = encoder.unapply(bitsRepr)

  override def isValid(n: Int): Boolean = (constraint(n))
}


/**
 * Quantizer for floating point value
 * @param encodingLength Number of bits representing the floating point value
 * @param constraint Constraint on the floating point value prior to conversion to bits sequence
 * @param scaleFactor Scaling factor applied to value prior to conversion
 */
class QuantizerDouble(
  override val encodingLength: Int,
  constraint: Double => Boolean,
  scaleFactor: Double) extends Quantizer[Double]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)

  /**
   * Convert a floating point value into a bits sequence
   *
   * @param tx floating point value to be converted
   * @throws GAException if the floating point input is out of range
   * @return Bits sequence
   */
  @throws(classOf[GAException])
  override def apply(x: Double): BitsRepr = {
    if(!constraint(x))
      throw new GAException(s"Value $x violates constraint of quantizer")
    encoder((scaleFactor*x).toInt)
  }

  override def unapply(bitsRepr: BitsRepr): Double = encoder.unapply(bitsRepr)/scaleFactor

  override def isValid(x: Double): Boolean = (constraint(x))
}


