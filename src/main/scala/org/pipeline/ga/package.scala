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
package org.pipeline

import scala.annotation.tailrec

package object ga{

  type BitsRepr = List[Int]

  final class GAException(msg: String) extends Exception(msg)

  final class BitsIntEncoder(encodingLength: Int){
    def apply(n: Int): BitsRepr = {
      @tailrec
      def encodeInt(n: Int, bits: BitsRepr, index: Int): BitsRepr = {
        if (index >= encodingLength) bits else {
          val bit = n & 0x01
          encodeInt(n >> 1, bit :: bits, index + 1)
        }
      }
      encodeInt(n, List[Int](), 0)
    }

    def unapply(bits: BitsRepr): Int = {
      @tailrec
      def decodeInt(bits: BitsRepr, index: Int, value: Int): Int = {
        if (index >= bits.length) value else {
          val newValue = if ((bits(index) & 0x01) == 0x01) value + (1 << index) else value
          decodeInt(bits, index + 1, newValue)
        }
      }
      decodeInt(bits.reverse, 0, 0)
    }
  }

  def toBits(n: Int, encodingLength: Int): BitsRepr =
    (0 until encodingLength).map(index => if((n & (1 << index)) == 0x01) 1 else 0).toList

  def repr(bitSet: java.util.BitSet, numBits: Int): BitsRepr =
    (0 until numBits).map(index => if (bitSet.get(index)) 1 else 0).toList



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
