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
package org.pipeline.ga


import scala.annotation.tailrec
import java.util


/**
 * Class that implements a parameterized chromosome using an encoding scheme and
 * an objective/fitness function. A chromosome is a container or list of Gene that
 * represents candidate solution to a problem or candidate model to a dataset.
 *
 * @tparam T Parameterized type for features
 * @constructor Create a chromosome with parameterized type for features
 * @throws IllegalArgumentException if the genetic code is undefined or empty
 * @param code List of features with parameterized type
 * @param encodingLength Number of bits to represent a features (Continuous or category)
 * @note This particular implementation computes the chromosome cost or unfitness.
 *       The fitness value of a chromosome is computes as 1/cost
 *
 * @author Patrick Nicolas
 */
@throws(classOf[IllegalArgumentException])
private[ga] class Chromosome[T : Quantizer](code: Seq[T], encodingLength: Int){
  import Chromosome._

  private[this] lazy val encoded: util.BitSet = {
    require(code.nonEmpty, "Chromosome Cannot create a chromosome from undefined genes")

    val bitsIntEncoder = new BitsIntEncoder(encodingLength)
    var index = 0
    val quantizer = implicitly[Quantizer[T]]
    code.foldLeft(new java.util.BitSet(code.length*encodingLength))(
      (bs, t) => {
        val value = quantizer(t)
        val bits = bitsIntEncoder(value)
        bits.foreach(
          bit => {
            bs.set(index, bit == 1)
            index += 1
          }
        )
        bs
      }
    )
  }

  /**
   * Decode a sequence of bits {0, 1} into a Chromosome
   * @param bitsSeq List of integers {0, 1} representing the bits
   * @return Instance of chromosome
   */
  def decode(bitsSeq: List[Int]): Chromosome[T] = {
    require(
      bitsSeq.size >= encodingLength,
      s"Failed to decode ${bitsSeq.size} bits should be >= ${encodingLength}"
    )

    val bitsIntEncoder = new BitsIntEncoder(encodingLength)
    val quantizer = implicitly[Quantizer[T]]

    val code: Seq[T] = (bitsSeq.indices by encodingLength).map(
      index => {
        val bits = bitsSeq.slice(index, index + encodingLength)
        val value = bitsIntEncoder.unapply(bits)
        quantizer.unapply(value)
      }
    )
    new Chromosome[T](code, encodingLength)
  }

  /**
   * Extract the bits representation for this Chromosome
   * @return Sequence of 1 or 0 as bit representation of this chromosome
   */
  def repr: Seq[Int] = Chromosome.repr(encoded)

  override def toString: String =
    s"${code.map(_.toString).mkString(" ")} with encoding length: $encodingLength"
}


/**
 * Companion object to a Chromosome used to define the constructors
 * @author Patrick Nicolas
 */
private[ga] object Chromosome {
  import java.util

  def apply[T : Quantizer](code: Seq[T], encodingLength: Int): Chromosome[T] =
    new Chromosome[T](code, encodingLength)

  def apply[T: Quantizer](encodingLength: Int): Chromosome[T] =
    new Chromosome[T](List.empty[T], encodingLength)


  class BitsIntEncoder(encodingLength: Int){
    def apply(n: Int): List[Int] = {
      @tailrec
      def encodeInt(n: Int, bits: List[Int], index: Int): List[Int] = {
        if(index >= encodingLength)
          bits
        else {
          val bit = n & 0x01
          encodeInt(n >> 1, bit :: bits, index + 1)
        }
      }
      encodeInt(n, List[Int](), 0)
    }

    def unapply(bits: List[Int]): Int = {
      @tailrec
      def decodeInt(bits: List[Int], index: Int, value: Int): Int = {
        if(index >= bits.length)
          value
        else {
          val newValue = if ((bits(index) & 0x01) == 0x01) value + (1 << index) else value
          decodeInt(bits, index + 1, newValue)
        }
      }
      decodeInt(bits.reverse, 0, 0)
    }
  }

  def repr(bitSet: util.BitSet): Seq[Int] =
    (0 until bitSet.length()).map(index => if(bitSet.get(index)) 1 else 0)
}