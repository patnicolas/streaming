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

import scala.annotation.switch
import scala.util.Random


/**
 *  Define the replication cycle in the execution of the genetic algorithm optimizer.
 *  A replication cycle consists of a selection of chromosomes according to their
 *  fitness/unfitness values, cross-over of pair of chromosomes and mutation
 *
 *  @constructor Create a reproduction cycle for the genetic algorithm.
 *  @tparam T type of gene (inherited from '''Gene''')
 *  @author Patrick Nicolas
 */
final private[ga] class Reproduction[T : Ordering, U: Ordering] protected (
  override val maxPopulationSize: Int,
  override val xOverProbThreshold: Double,
  override val mutationProbThreshold: Double,
  xOverStrategy: String
) extends SelectionOp with XOverOp with MutationOp {
  private[this] val rand = new Random(System.currentTimeMillis)

  def mate(chromosomes: Seq[Chromosome[T, U]]): Seq[Chromosome[T, U]] = {
    val selectedChromosomes = apply(chromosomes)   // Selection
    val offSprings = apply(selectedChromosomes, xOverStrategy)
    (chromosomes ++ offSprings).map(apply(_))
  }

  def apply(chromosomes: Seq[Chromosome[T, U]]): Seq[Chromosome[T, U]] = ???

}

/**
 * Companion object for the Reproduction class. This singleton is used
 * to define the default constructor of the Reproduction class.
 * @author Patrick Nicolas
 */
private[ga] object Reproduction {

}



