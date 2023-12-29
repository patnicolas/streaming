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

  final def getFeatures2: Seq[Gene[U]] = features2


  def mutate(mutationOp: MutationOp): Chromosome[T, U] = mutationOp(this)

  def mutate(mutationProb: Double): Chromosome[T, U] = {
    require(
      mutationProb >= 1e-5 && mutationProb <= 0.9,
      s"Mutation probability $mutationProb is out of range [1e-5, 0.9]"
    )
    (new MutationOp{
      override val mutationProbThreshold: Double = mutationProb
    })(this)
  }

  private[this] lazy val encoded: util.BitSet = {
    require(
      features1.nonEmpty || features2.nonEmpty,
      "Chromosome Cannot create a chromosome from undefined genes"
    )

    val bitsSequence = features1.flatMap(_.getBitsSequence) ++ features2.flatMap(_.getBitsSequence)
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

  override def toString: String =
    (features1.map(_.getValue.toString) ++ features2.map(_.getValue.toString)).mkString(" ")
}


/**
 * Companion object to a Chromosome used to define the constructors
 * @author Patrick Nicolas
 */
private[ga] object Chromosome {

  def apply[T](features: Seq[Gene[T]]): Chromosome[T, T] =
    new Chromosome[T, T](features, Seq.empty[Gene[T]])

  def apply[T, U](features1: Seq[Gene[T]], features2: Seq[Gene[U]]): Chromosome[T, U] =
    new Chromosome[T, U](features1, features2)

  def apply[T, U](): Chromosome[T, U] =
    new Chromosome[T, U](Seq.empty[Gene[T]], Seq.empty[Gene[U]])

}