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

trait Quantizer[T]{
  val encodingLength: Int

  def apply(t: T): BitsRepr

  def unapply(bitsRepr: BitsRepr): T

  def isValid(t: T): Boolean
}

class QuantizerBool(
  override val encodingLength: Int
) extends Quantizer[Boolean]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)
  override def apply(t: Boolean): BitsRepr = {
    val n: Int = if (t) 1 else 0
    encoder(n)
  }

  override def unapply(bitsRepr: BitsRepr): Boolean = encoder.unapply(bitsRepr) > 0

  override def isValid(b: Boolean): Boolean = true
}

class QuantizerInt(
  override val encodingLength: Int,
  constraint: Int => Boolean) extends Quantizer[Int]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)

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
 *
 * @param encodingLength
 * @param constraint
 * @param scaleFactor
 */
class QuantizerDouble(
  override val encodingLength: Int,
  constraint: Double => Boolean,
  scaleFactor: Double) extends Quantizer[Double]{

  private[this] val encoder = new BitsIntEncoder(encodingLength)

  @throws(classOf[GAException])
  override def apply(x: Double): BitsRepr = {
    if(!constraint(x))
      throw new GAException(s"Value $x violates constraint of quantizer")
    encoder((scaleFactor*x).toInt)
  }

  override def unapply(bitsRepr: BitsRepr): Double = encoder.unapply(bitsRepr)/scaleFactor

  override def isValid(x: Double): Boolean = (constraint(x))
}


