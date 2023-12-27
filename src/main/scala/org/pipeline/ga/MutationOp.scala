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

import scala.util.Random
import java.util
/**
 *
 */
trait MutationOp extends GAOp {
self =>
  protected[this] val mutationProbThreshold: Double

  def apply[T](gene: Gene[T]): Gene[T] = {
    if(rand.nextDouble < mutationProbThreshold) {
      val flippedBitSet = flip(gene.getEncoded,  gene.size())
      val newValue = gene.getValidValue(flippedBitSet)
      Gene[T](newValue, gene.getQuantizer, mutationProbThreshold)
    }
    else
      gene
  }


  def apply[T, U](chromosome: Chromosome[T, U]): Chromosome[T, U] =
    if(rand.nextDouble < mutationProbThreshold) {
      val features1 = chromosome.getFeatures1
      val features2 = chromosome.getFeatures2
      val chromosomeLength: Int = features1.length + features2.length

      val geneIndex = (chromosomeLength* Random.nextDouble).toInt + 1

      if(geneIndex < features1.length) {
        val geneToMutate = features1(geneIndex)
        features1.updated(geneIndex, apply(geneToMutate))
      }
      else {
        val relativeIndex = geneIndex - features1.length
        val geneToMutate = features2(relativeIndex)
        features2.updated(relativeIndex, apply(geneToMutate))
      }

      Chromosome[T, U](features1, features2)
    }
    else
      chromosome

  private def flip(bitSet: util.BitSet, encodingLength: Int): util.BitSet = {
    val bitSetIndex = (encodingLength * Random.nextDouble).toInt + 1
    bitSet.flip(bitSetIndex)
    bitSet
  }
}
