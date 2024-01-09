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
import scala.util.Random

/**
 * Implements Cross-over for Chromosomes with heterogeneous features (Gene types)
 * xOverProbThreshold: Threshold value for the probability to trigger a cross-over
 * @author Patrick Nicolas
 */
private[ga] trait XOverOp extends GAOp {
self =>
  import XOverOp._
  protected[this] val xOverProbThreshold: Double



  /**
   * Implements the XOver operator between two chromosomes
   * @param chromosome1 First parent chromosome
   * @param chromosome2 Second parent chromosome
   * @tparam T Type of first set of genes for each chromosome
   * @tparam U Type of second set of genes for each chromosome
   * @return Pair of off spring chromosomes
   */
  def xOver[T : Ordering, U : Ordering](
    chromosome1: Chromosome[T, U],
    chromosome2: Chromosome[T, U]
  ): (Chromosome[T, U], Chromosome[T, U]) = {

    // if the Cross-over is triggered
    if(rand.nextDouble < xOverProbThreshold) {
      val xOverIndex = (chromosome1.size()*Random.nextDouble).toInt
      val features1Len = chromosome1.getFeaturesT.length

      // The cross-over cut-off is done within the first set of genes, preserving
      // the second set of genes ..
      if(xOverIndex < features1Len)
        xOverFirstFeatures(chromosome1, chromosome2)
        // Otherwise the cross-over is performed within the second set of genes/features
      else
        xOverSecondFeatures(chromosome1, chromosome2)
    }
    else
      (chromosome1, chromosome2)
  }

  /**
   * Implements the following crossover strategies for chromosomes ranked by their decreasing
   * order of fitness.
   * - midPoint for which the pair of parent ranked chromosomes are selected across the mid point
   * - pairing for which the pair of parent ranked chromosomes are selected consecutively
   * - random for which the pair of parent chromosomes are selected randomly
   * @param chromosomes Parent chromosomes
   * @param xOverStrategy Crossover strategy (MidPoint, Pairing, Random)
   * @tparam T Type of the first set of features
   * @tparam U Type of the second set of features
   * @return Parent and offspring chromosomes
   */
  def xOver[T : Ordering, U : Ordering](
    chromosomes: Seq[Chromosome[T, U]],
    xOverStrategy: String
  ): Seq[Chromosome[T, U]] = xOverStrategy match {
    case `midPointStrategy` =>
      val midPoint = chromosomes.length >> 1
      val (topChromosomes, botChromosomes) = chromosomes.splitAt(midPoint)
      val (offSprings1, offSpring2) = (0 until midPoint).map(
        index => xOver(topChromosomes(index), botChromosomes(index))
      ).unzip
      chromosomes ++ offSprings1 ++ offSpring2

    case `pairingStrategy` =>
      chromosomes ++ pairingChromosomes(chromosomes)

    case `randomStrategy` =>
      import scala.util.Random._
      val shuffledChromosomes = shuffle(chromosomes)
      chromosomes ++ pairingChromosomes(shuffledChromosomes)

    case _ => throw new GAException(s"XOver strategy $xOverStrategy is not supported")
  }


  def apply(bitSet1: util.BitSet, bitSet2: util.BitSet, encodingSize: Int): (util.BitSet, util
  .BitSet) =
    if(rand.nextDouble < xOverProbThreshold) {
      val xOverIndex = (encodingSize*Random.nextDouble).toInt
      val bitSet1Top = bitSet1.get(0, xOverIndex)
      val bitSet2Top = bitSet2.get(0, xOverIndex)
      val bitSet1Bottom = bitSet1.get(xOverIndex, encodingSize)
      val bitSet2Bottom = bitSet2.get(xOverIndex, encodingSize)
      val offSpring1 = XOverOp.xOver(bitSet1Top, xOverIndex, bitSet2Bottom, encodingSize)
      val offSpring2 = XOverOp.xOver(bitSet2Top, xOverIndex, bitSet1Bottom, encodingSize)

      (offSpring1, offSpring2)
    }
    else
      (bitSet1, bitSet2)

  // ----------------------  Helper methods -----------------------------------

  @throws(clazz = classOf[GAException])
  private def validate(): Unit =
    if (xOverProbThreshold < 0.0 || xOverProbThreshold > 0.7)
      throw new GAException(
        s"Cross-over probability threshold $xOverProbThreshold should be [0.0, 0.7]")


  private def pairingChromosomes[T: Ordering, U: Ordering](
    chromosomes: Seq[Chromosome[T, U]]
  ): Seq[Chromosome[T, U]] = {
    val (offSprings1, offSpring2) = (chromosomes.indices by 2).map(
      index => xOver(chromosomes(index), chromosomes(index + 1))
    ).unzip
    offSprings1 ++ offSpring2
  }

}


/**
 * Companion object to implement the sub routines for the cross-over for chromosome
 * with heterogeneous genes/features types.
 * @author Patrick Nicolas
 */
private[ga] object XOverOp {

  final val midPointStrategy = "midPoint"
  final val pairingStrategy = "pairing"
  final val randomStrategy = "random"

  private def xOverFirstFeatures[T : Ordering, U : Ordering](
    chromosome1: Chromosome[T, U],
    chromosome2: Chromosome[T, U]
  ): (Chromosome[T, U], Chromosome[T, U]) = {
    val xOverIndex = (chromosome1.size() * Random.nextDouble).toInt
    val offSpring1 = features1OffSpring(
      chromosome1.getFeaturesT,
      chromosome2.getFeaturesT,
      chromosome2.getFeaturesU,
      xOverIndex)
    val offSpring2 = features1OffSpring(
      chromosome2.getFeaturesT,
      chromosome1.getFeaturesT,
      chromosome1.getFeaturesU,
      xOverIndex)
    (offSpring1, offSpring2)
  }

  private def features1OffSpring[T : Ordering, U : Ordering](
    features1: Seq[Gene[T]],
    features2: Seq[Gene[T]],
    features3: Seq[Gene[U]],
    xOverIndex: Int): Chromosome[T, U] = {
    val topChromosome1Genes = features1.slice(0, xOverIndex + 1)
    val botChromosome2Genes = features2.slice(xOverIndex, features1.length)

    Chromosome[T, U](topChromosome1Genes ++ botChromosome2Genes, features3)
  }


  private def xOverSecondFeatures[T : Ordering, U : Ordering](
    chromosome1: Chromosome[T, U],
    chromosome2: Chromosome[T, U]): (Chromosome[T, U], Chromosome[T, U]) = {
    val xOverIndex = (chromosome1.size() * Random.nextDouble).toInt
    val relativeIndex = xOverIndex - chromosome1.getFeaturesT.length

    val offSpring1 = features2OffSpring(
      chromosome1.getFeaturesU,
      chromosome2.getFeaturesU,
      chromosome2.getFeaturesT,
      relativeIndex)
    val offSpring2 = features2OffSpring(
      chromosome2.getFeaturesU,
      chromosome1.getFeaturesU,
      chromosome1.getFeaturesT,
      relativeIndex)
    (offSpring1, offSpring2)
  }

  private def features2OffSpring[T : Ordering, U : Ordering](
    features1: Seq[Gene[U]],
    features2: Seq[Gene[U]],
    features3: Seq[Gene[T]],
    relativeIndex: Int): Chromosome[T, U] = {
    val topChromosome1Genes = features1.slice(0, relativeIndex + 1)
    val botChromosome2Genes = features2.slice(relativeIndex, features2.length)

    Chromosome[T, U](features3, topChromosome1Genes ++ botChromosome2Genes)
  }

  private def xOver(
    bitSet1: util.BitSet,
    encoding1Size: Int,
    bitSet2: util.BitSet,
    encodingSize: Int): util.BitSet = {

    (encoding1Size until encodingSize).foldLeft(bitSet1)(
      (offSpring, index) => {
        offSpring.set(index, bitSet2.get(index))
        offSpring
      }
    )
  }
}
