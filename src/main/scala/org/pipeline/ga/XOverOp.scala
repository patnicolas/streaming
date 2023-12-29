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
 *
 */
private[ga] trait XOverOp extends GAOp {
self =>
  protected[this] val xOverProbThreshold: Double

  /**
   * Implements the XOver operator between two chromosomes
   * @param chromosome1 First parent chromosome
   * @param chromosome2 Second parent chromosome
   * @tparam T Type of first set of genes for each chromosome
   * @tparam U Type of second set of genes for each chromosome
   * @return Pair of off spring chromosomes
   */
  def apply[T, U](
    chromosome1: Chromosome[T, U],
    chromosome2: Chromosome[T, U]
  ): (Chromosome[T, U], Chromosome[T, U]) = {

    // if the Cross-over is triggered
    if(rand.nextDouble < xOverProbThreshold) {
      val xOverIndex = (chromosome1.size()*Random.nextDouble).toInt
      val features1Len = chromosome1.getFeatures1.length

      // The cross-over cut-off is done within the first set of genes, preserving
      // the second set of genes ..
      if(xOverIndex < features1Len) {
        val topChromosome1Genes = chromosome1.getFeatures1.slice(0, xOverIndex+1)
        val botChromosome2Genes  = chromosome2.getFeatures1.slice(xOverIndex, features1Len)
        val offSpring1 = Chromosome[T, U](
          topChromosome1Genes ++ botChromosome2Genes,
          chromosome2.getFeatures2
        )

        val topChromosome2Genes = chromosome2.getFeatures1.slice(0, xOverIndex+1)
        val botChromosome1Genes =  chromosome1.getFeatures1.slice(xOverIndex, features1Len)
        val offSpring2 = Chromosome[T, U](
          topChromosome2Genes ++ botChromosome1Genes,
          chromosome1.getFeatures2
        )
        (offSpring1, offSpring2)
      }

        // Otherwise the cross-over is performed within the second set of genes/features
      else {
        val features2Len = chromosome2.getFeatures2.length
        val relativeIndex = xOverIndex - features1Len
        val topChromosome1Genes = chromosome1.getFeatures2.slice(0, relativeIndex + 1)
        val botChromosome2Genes = chromosome2.getFeatures2.slice(relativeIndex, features2Len)
        val offSpring1 = Chromosome[T, U](
          chromosome2.getFeatures1,
          topChromosome1Genes ++ botChromosome2Genes
        )

        val topChromosome2Genes = chromosome2.getFeatures2.slice(0, relativeIndex + 1)
        val botChromosome1Genes = chromosome1.getFeatures2.slice(relativeIndex, features2Len)

        val offSpring2 = Chromosome[T, U](
          chromosome1.getFeatures1,
          topChromosome2Genes ++ botChromosome1Genes
        )
        (offSpring1, offSpring2)
      }
    }
    else
      (chromosome1, chromosome2)
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
}


private[ga] object XOverOp {
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
