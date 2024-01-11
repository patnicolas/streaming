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

import org.pipeline.streams.spark.SparkConfiguration


/**
 * Implements the selection operator. The main method, apply, rank the chromosomes by their
 * fitness and select the top chromosomes constrained by the maximum allowed size of the population.
 * maxPopulationSize Maximum size allowed for the number of chromosomes across the reproduction
 *                   cycle
 *
 * @author Patrick Nicolas
 */
trait SelectionOp{
self =>
  protected[this] val maxPopulationSize: Int


  /**
   * Initial random initialization of the population of Chromosomes
   * @param featureTIds Identifiers for features of first type (Integer, Floating point,...)
   * @param gaTEncoders Encoder for the features of first type
   * @param featureUIds Identifiers for features of second type (Integer, Floating point,...)
   * @param gaUEncoders Encoder for the features of second type
   * @tparam T Type of first set of features
   * @tparam U Type of second set of features
   * @return Random instance of a chromosome
   */
  def rand[T : Ordering, U : Ordering](
    featureTIds: Seq[String],
    gaTEncoders: Seq[GAEncoder[T]],
    featureUIds: Seq[String],
    gaUEncoders: Seq[GAEncoder[U]]
  ): Seq[Chromosome[T, U]] = {
    validate()
    Seq.tabulate(maxPopulationSize)(
      index =>
        Chromosome.rand(featureTIds, gaTEncoders, featureUIds, gaUEncoders)
    )
  }

  /**
   * Order/Rank a sequence of chromosome by their fitness value, then select a subset
   * @param chromosomes Population of chromosomes.
   * @tparam T Built-in type for the first set of features
   * @tparam U Built-in type for the second set of features
   * @return Ranked and trimmed the current population of chromosomes
   */
  def select[T : Ordering, U : Ordering](
    chromosomes: Seq[Chromosome[T, U]]): Seq[Chromosome[T, U]] =
    chromosomes.sortWith(_.fitness > _.fitness).take(maxPopulationSize)


  // ------------------- Helper methods ------------------------------

  @throws(clazz = classOf[GAException])
  private def validate(): Unit =
    if (maxPopulationSize < 0 || maxPopulationSize > 1024)
      throw new GAException(
        s"Selection max population size $maxPopulationSize should be [0, 1024]")
}
