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


import org.pipeline.ga.Chromosome.BitsIntEncoder
import org.pipeline.ga.Gene.GeneIndexer

import java.util
import scala.annotation.{implicitNotFound, tailrec}
import scala.collection.mutable
import scala.util.Random


/**
 * Class that implements a parameterized chromosome using an encoding scheme and
 * an objective/fitness function. A chromosome is a container or list of Gene that
 * represents candidate solution to a problem or candidate model to a dataset.
 *
 * @tparam T Parameterized type upper bounded by '''Gene'''
 * @constructor Create a chromosome with the parameterized sub-type of Gene
 * @throws IllegalArgumentException if the genetic code is undefined or empty
 * @param code List of Genes or sub types composing this chromosomes.
 * @author Patrick Nicolas
 * @note This particular implementation computes the chromosome cost or unfitness.
 *       The fitness value of a chromosome is computes as 1/cost
 */
@throws(classOf[IllegalArgumentException])
private[ga] final class Chromosome[T](code: Seq[T], encoding: T => Int){
  require(code.nonEmpty, "Chromosome Cannot create a chromosome from undefined genes")

  val bitsIntEncoder = new BitsIntEncoder(6)
  private[this] val encoded: java.util.BitSet = code.foldLeft(new java.util.BitSet)(
    (bs, t) => {
      val value = encoding(t)
      val bits = bitsIntEncoder(value)
      var index = bs.length
      bits.foreach(
        bit => {
          bs.set(index, bit)
          index += 1
        }
      )
      bs
    }
  )
}


/**
 * Companion object to a Chromosome used to define the constructors
 * @author Patrick Nicolas
 */
private[ga] object Chromosome {

  class BitsIntEncoder(maxNumBits: Int){
    def apply(n: Int): List[Int] = {
      @tailrec
      def encodeInt(n: Int, bits: List[Int], index: Int): List[Int] = {
        if(index >= maxNumBits)
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
}