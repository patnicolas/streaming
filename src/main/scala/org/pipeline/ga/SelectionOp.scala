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

trait SelectionOp{
self =>
  protected[this] val maxPopulationSize: Int

  /**
   * Initial random initialization of the population of Chromosomes
   * @param numChromosomes Initial size of the population
   * @param numFirstGenes Number of features of first type (Integer, Floating point,...)
   * @param quantizer1 Quantizer for the features of first type
   * @param numSecondGenes Number of features of second type (Integer, Floating point,...)
   * @param quantizer2 Quantizer for the features of second type
   * @tparam T Type of first set of features
   * @tparam U Type of second set of features
   * @return Random instance of a chromosome
   */
  def apply[T : Ordering, U : Ordering](
    numChromosomes: Int,
    numFirstGenes: Int,
    quantizer1: Quantizer[T],
    numSecondGenes: Int,
    quantizer2: Quantizer[U]
  ): Seq[Chromosome[T, U]] =
    Seq.fill(
      numChromosomes
    )(
      Chromosome.rand(numFirstGenes, quantizer1, numSecondGenes, quantizer2)
    )

  def apply[T : Ordering, U : Ordering](chromosomes: Seq[Chromosome[T, U]]): Seq[Chromosome[T, U]] =
    chromosomes.sortWith(_.fitness > _.fitness).take(maxPopulationSize)
}
