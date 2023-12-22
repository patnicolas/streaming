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
package org.pipeline


package object ga{

  def repr(bitSet: java.util.BitSet, numBits: Int): List[Int] =
    (0 until numBits).map(index => if (bitSet.get(index)) 1 else 0).toList

  trait Quantizer[T] {
    def apply(t: T): Int

    def unapply(n: Int): T
  }

  implicit val defaultQuantizer: Quantizer[Int] = new  Quantizer[Int] {
    def apply(t: Int): Int = t

    def unapply(n: Int): Int = n
  }


  /**
   * Generic operator for symbolic representation of a gene defined
   * as a tuple {variable, operator, target_value}. An operator can be logical (OR, AND, NOT)
   * or numeric (>, <, ==). Symbolic operators should not be confused with
   * genetic operators such as mutation or cross-over.
   *
   * @author Patrick Nicolas
   */
  private[ga] trait GAOp{
    import scala.util.Random
    /**
     * Identifier for the operator of type Integer
     *
     * @return operator unique identifier
     */
    protected[this] val rand = new Random(42L)
  }



  /**
   * Class for the conversion between time series with discrete values (digital of type Int)
   * and time series with continuous values (analog of type Double). Continuous values
   * are digitized over an interval through a linear segmentation.
   *
   * A continuous time series with minimum value, m and maximum value M is quantized over
   * an interval [a, b]
   * {{{
   *  x [a, b]  x -> (x - m)*(b - a)/(M- n) + a
   *  }}
   * @constructor Quantization class that convert a Double to Int and an Int to a Double.
   * @param toInt Function which categorizes a continuous signal or pseudo-continuous data set
   * @param toDouble convert a categorizes time series back to its original (continuous) values
   * @author Patrick Nicolas
   */
  case class Quantization[U](toInt: U => Int, toU: Int => U)

  final val NoQuantization = Quantization[Double](
    (u: Double) => u.toInt,
    (n: Int) => n.toDouble
  )
}
