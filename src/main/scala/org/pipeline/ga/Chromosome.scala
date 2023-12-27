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

import org.pipeline.ga
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
 * @note This particular implementation computes the chromosome cost or unfitness.
 *       The fitness value of a chromosome is computes as 1/cost
 *
 * @author Patrick Nicolas
 */
@throws(classOf[IllegalArgumentException])
private[ga] class Chromosome[T, U] private (features1: Seq[Gene[T]], features2: Seq[Gene[U]]){

  final def getFeatures1: Seq[Gene[T]] = features1

  final def getFeatures2: Seq[Gene[T]] = features2

  private[this] lazy val encoded: util.BitSet = {
    require(
      features1.nonEmpty || features2.nonEmpty,
      "Chromosome Cannot create a chromosome from undefined genes"
    )

    val bitsSequence = code.flatMap(_.getBitsSequence)
    val numBits = bitsSequence.length
    val bitSet = new java.util.BitSet(numBits)
    (0 until  numBits).foreach(
      index =>
        bitSet.set(index, bitsSequence(index) == 1)
    )
    bitSet
  }

  /**
   * Decode a sequence of bits {0, 1} into a Chromosome
   * @param bitsSequence List of integers {0, 1} representing the bits
   * @return Instance of chromosome
   */
    /*
  def decode(bitsSequence: BitsRepr, encodingLength: Int): Chromosome[T] = {
    require(
      bitsSequence.size >= encodingLength,
      s"Failed to decode ${bitsSequence.size} bits should be >= $encodingLength"
    )
    val gene = Gene[T](encodingLength)
    val code = (bitsSequence.indices by encodingLength).map(
      index => {
        val bitsSlice: BitsRepr = bitsSequence.slice(index, index + encodingLength)
        gene.decode(bitsSlice)
      }
    )
    new Chromosome[T](code)
  }

     */

  /**
   *
   * @param bitSet
   * @param encodingLength
   * @return
   */
    /*
  def decode(bitSet: util.BitSet, encodingLength: Int): Chromosome[T] = {
    val bitsSequence = ga.repr(bitSet, encodingLength*code.length)
    decode(bitsSequence, encodingLength)
  }

     */

  final def getEncoded: util.BitSet = encoded

  /**
   * Extract the bits representation for this Chromosome
   * @return Sequence of 1 or 0 as bit representation of this chromosome
   */
  def repr: Seq[Int] = ga.repr(encoded, code.head.size()*code.length)

  override def toString: String =
    code.map(_.getValue.toString).mkString(" ")
}


/**
 * Companion object to a Chromosome used to define the constructors
 * @author Patrick Nicolas
 */
private[ga] object Chromosome {

  def apply[T](code: Seq[Gene[T]]): Chromosome[T] = new Chromosome[T](code)

  /*
  def apply[T](elements: Seq[T], encodingLength: Int): Chromosome[T] = {
    val genes = elements.map(Gene[T](_, encodingLength))
    new Chromosome[T](genes)
  }

   */

  def apply[T](): Chromosome[T] = new Chromosome[T](Seq.empty[Gene[T]])

}