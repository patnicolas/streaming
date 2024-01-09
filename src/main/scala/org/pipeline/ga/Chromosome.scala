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

import java.util


/**
 * Class that implements a parameterized chromosome using an encoding scheme and
 * an objective/fitness function. A chromosome is a container or list of Gene that
 * represents candidate solution to a problem or candidate model to a dataset.
 *
 * @tparam T Parameterized type for features (i.e. Int, Float,...)
 * @tparam U Parameterized type for features (i.e. Int, Float,...)
 * @constructor Create a chromosome with parameterized type for features
 * @throws IllegalArgumentException if the genetic code is undefined or empty
 * @param featuresT List of features with parameterized type T
 * @param featuresU List of features with parameterized type U
 * @note This particular implementation computes the chromosome cost or unfitness.
 *       The fitness value of a chromosome is computes as 1/cost
 *
 * @author Patrick Nicolas
 */
@throws(classOf[IllegalArgumentException])
private[ga] class Chromosome[T : Ordering, U : Ordering] private (
  featuresT: Seq[Gene[T]],
  featuresU: Seq[Gene[U]]){

  var fitness: Double = -1.0

  final def getFeaturesT: Seq[Gene[T]] = featuresT

  final def getFeaturesU: Seq[Gene[U]] = featuresU


  /**
   * Implements a cross-over with another chromosome. The operation generates two offsprings.
   * @param otherChromosome The second 'parent' Chromosome
   * @param xOverOp Cross-over operator
   * @return Pair of offspring chromosomes
   */
  def xOver(
    otherChromosome: Chromosome[T, U],
    xOverOp: XOverOp): (Chromosome[T, U], Chromosome[T, U]) = xOverOp.xOver(this, otherChromosome)

  /**
   * Implements a cross-over with another chromosome. The operation generates two offsprings.
   * @param otherChromosome The second 'parent' Chromosome
   * @param xOverThreshold Threshold for the probability to trigger a cross-over of this
   *                       chromosome with another one.
   * @return Pair of offspring chromosomes
   */
  def xOver(
    otherChromosome: Chromosome[T, U],
    xOverThreshold: Double): (Chromosome[T, U], Chromosome[T, U]) = {
    require(
      xOverThreshold >= 1e-5 && xOverThreshold <= 0.9,
      s"Cross-over probability $xOverThreshold is out of range [1e-5, 0.9]")

    (new XOverOp{
      override val xOverProbThreshold: Double = xOverThreshold
    }).xOver(chromosome1 = this, otherChromosome)
  }

  def mutate(mutationOp: MutationOp): Chromosome[T, U] = mutationOp.mutate(chromosome = this)

  def mutate(mutationProb: Double): Chromosome[T, U] = {
    require(
      mutationProb >= 1e-5 && mutationProb <= 0.9,
      s"Mutation probability $mutationProb is out of range [1e-5, 0.9]"
    )
    (new MutationOp{
      override val mutationProbThreshold: Double = mutationProb
    }).mutate(chromosome = this)
  }

  private[this] lazy val encoded: util.BitSet = {
    require(
      featuresT.nonEmpty || featuresU.nonEmpty,
      "Chromosome Cannot create a chromosome from undefined genes"
    )

    val bitsSequence = featuresT.flatMap(_.getBitsSequence) ++ featuresU.flatMap(_.getBitsSequence)
    val numBits = bitsSequence.length
    val bitSet = new java.util.BitSet(numBits)
    (0 until  numBits).foreach(
      index =>
        bitSet.set(index, bitsSequence(index) == 1)
    )
    bitSet
  }

  final def size(): Int = featuresT.length + featuresU.length

  final def getEncoded: util.BitSet = encoded

  override def toString: String =
    (featuresT.map(_.getValue.toString) ++ featuresU.map(_.getValue.toString)).mkString(" ")
}


/**
 * Companion object to a Chromosome used to define the constructors
 * @author Patrick Nicolas
 */
private[ga] object Chromosome {

  def apply[T : Ordering](features: Seq[Gene[T]]): Chromosome[T, T] =
    new Chromosome[T, T](features, Seq.empty[Gene[T]])

  def apply[T : Ordering, U : Ordering](
    featuresT: Seq[Gene[T]],
    featuresU: Seq[Gene[U]]): Chromosome[T, U] = new Chromosome[T, U](featuresT, featuresU)

  /**
   * Generate an initial, random Chromosome
   * @param featureTIds           List of identifiers for the features of first type
   * @param gaTEncoders     Quantizer associated with the first type of features
   * @param featureUIds           List of identifiers for the features of second type
   * @param gaUEncoders     Quantizer associated with the second type of features
   * @tparam T Built-in type for the first set of features
   * @tparam U Built-in type for the second set of features
   * @return Initialized instance of a Chromosome */
  def apply[T : Ordering, U : Ordering](
    featureTIds: Seq[String],
    gaTEncoders: Seq[GAEncoder[T]],
    featureUIds: Seq[String],
    gaUEncoders: Seq[GAEncoder[U]]): Chromosome[T, U] =
    rand[T, U](featureTIds, gaTEncoders, featureUIds, gaUEncoders)

  /**
   * Generate an initial, random Chromosome
   * @param featureTIds List of identifiers fpr features of first type (Int, Float,...)
   * @param gaTEncoders Quantizer associated with the first type of features
   * @param featureUIds List of identifier for features of second type
   * @param gaUEncoders Quantizer associated with the second type of features
   * @tparam T Built-in type for the first set of features
   * @tparam U Built-in type for the second set of features
   * @return Initialized instance of a Chromosome
   */
  @throws(clazz = classOf[IllegalArgumentException])
  def rand[T : Ordering, U : Ordering](
    featureTIds: Seq[String],
    gaTEncoders: Seq[GAEncoder[T]],
    featureUIds: Seq[String],
    gaUEncoders: Seq[GAEncoder[U]]): Chromosome[T, U] = {
    require(
      featureTIds.length == gaTEncoders.length,
      s"Number features ${featureTIds.length} should be == number of encoder ${gaTEncoders.length}"
    )
    require(
      featureUIds.length == gaUEncoders.length,
      s"Number features ${featureTIds.length} should be == number of encoder ${gaTEncoders.length}"
    )

    val featuresT = Seq.tabulate(featureTIds.length)(
      index => Gene[T](featureTIds(index), gaTEncoders(index))
    )
    val featuresU = Seq.tabulate(featureUIds.length)(
      index => Gene[U](featureUIds(index), gaUEncoders(index))
    )

    new Chromosome[T, U](featuresT, featuresU)
  }
}