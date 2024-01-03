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


/**
 *
 */
trait SelectionOp{
self =>
  protected[this] val maxPopulationSize: Int

  /**
   * Initial random initialization of the population of Chromosomes
   * @param idsT Identifiers for features of first type (Integer, Floating point,...)
   * @param gaEncoder1 Encoder for the features of first type
   * @param idsU Identifiers for features of second type (Integer, Floating point,...)
   * @param gaEncoder2 Encoder for the features of second type
   * @tparam T Type of first set of features
   * @tparam U Type of second set of features
   * @return Random instance of a chromosome
   */
  def apply[T : Ordering, U : Ordering](
    idsT: Seq[String],
    gaEncoder1: GAEncoder[T],
    idsU: Seq[String],
    gaEncoder2: GAEncoder[U]
  ): Seq[Chromosome[T, U]] =
    Seq.fill(maxPopulationSize)(Chromosome.rand(idsT, gaEncoder1, idsU, gaEncoder2))

  /**
   * Order/Rank a sequence of chromosome by their fitness value, then select a subset
   * @param chromosomes Population of chromosomes.
   * @tparam T Built-in type for the first set of features
   * @tparam U Built-in type for the second set of features
   * @return Ranked and trimmed the current population of chromosomes
   */
  def apply[T : Ordering, U : Ordering](chromosomes: Seq[Chromosome[T, U]]): Seq[Chromosome[T, U]] =
    chromosomes.sortWith(_.fitness > _.fitness).take(maxPopulationSize)
}
