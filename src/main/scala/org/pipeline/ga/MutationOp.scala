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
import scala.annotation.tailrec

/**
 * Mutation operator to be applied to Genes and Chromosomes (as defined has a sequence
 * of heterogeneous genes
 * mutationProbThreshold: Threshold for triggering a mutation
 *
 * @author Patrick Nicolas
 */
trait MutationOp extends GAOp {
self =>
  protected[this] val mutationProbThreshold: Double
  private[this] val rand = new Random(42L)

  /**
   *
   * @param gene
   * @tparam T
   * @return
   */
  def apply[T: Ordering](gene: Gene[T]): Gene[T] = {
    if(rand.nextDouble < mutationProbThreshold) {
      val newValue = createValidMutation(gene, implicitly[Ordering[T]])
      Gene[T](gene.getId, newValue, gene.getQuantizer)
    }
    else
      gene
  }

  @tailrec
  private def createValidMutation[T: Ordering](gene: Gene[T], ord: Ordering[T]): T = {
    val flippedBitSet: util.BitSet = flip(gene.getEncoded, gene.size())
    val newValue = gene.getValidValue(flippedBitSet)
    val isValid = ord.lt(newValue, gene.getQuantizer.maxValue)
    if(isValid) newValue else createValidMutation(gene, ord)
  }


  /**
   *
   * @param chromosome
   * @tparam T
   * @tparam U
   * @return
   */
  def apply[T : Ordering, U: Ordering](chromosome: Chromosome[T, U]): Chromosome[T, U] =
    if(rand.nextDouble < mutationProbThreshold) {
      val features1 = chromosome.getFeatures1
      val features2 = chromosome.getFeatures2
      val chromosomeLength: Int = features1.length + features2.length

      val geneIndex = (chromosomeLength* Random.nextDouble).toInt

      // If the index of the gene to mutate is within the first set of features or
      // if there is only one set of features of same type..
      if(geneIndex < features1.length || features2.isEmpty) {
        val geneToMutate = features1(geneIndex)
        val mutatedGene: Gene[T] = apply(geneToMutate)
        features1.updated(geneIndex, mutatedGene)
      }
        // Otherwise if the mutation has to be performed on the second set of features....
      else {
        val relativeIndex = geneIndex - features1.length
        val geneToMutate: Gene[U] = features2(relativeIndex)
        val mutatedGene: Gene[U] = apply(geneToMutate)
        features2.updated(relativeIndex, mutatedGene)
      }

      Chromosome[T, U](features1, features2)
    }
    else
      chromosome

  private def flip(bitSet: util.BitSet, encodingLength: Int): util.BitSet = {
    val bitSetIndex = (encodingLength * rand.nextDouble).toInt
    bitSet.flip(bitSetIndex)
    bitSet
  }
}
