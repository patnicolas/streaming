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
/*
import scala.annotation.switch
import scala.util.Random


/**
 *  Define the replication cycle in the execution of the genetic algorithm optimizer.
 *  A replication cycle consists of a selection of chromosomes according to their
 *  fitness/unfitness values, cross-over of pair of chromosomes and mutation
 *
 *  @constructor Create a reproduction cycle for the genetic algorithm.
 *  @tparam T type of gene (inherited from '''Gene''')
 *  @param score Scoring function of a chromosome (unfitness of the candidate solution)
 *  @author Patrick Nicolas
 */
final private[ga] class Reproduction[U, T <: Gene[U]] protected (
  score: Chromosome[U, T] => Unit
) {
  private[this] val rand = new Random(System.currentTimeMillis)

  /**
   * Execute the 3 phases of the genetic replication: Selection, Cross-over and Mutation.
   * @param population current population of chromosomes used in the replication process
   * @param configurator configuration of the genetic algorithm.
   * @param cycle Current reproduction cycle number
   * @return true if the selection, crossover and mutation phases succeed, None otherwise.
   */
  def mate(
    population: Population[U, T],
    configurator: GAConfigurator,
    cycle: Int
  ): Boolean = (population.size: @switch) match {

    // If the population has less than 3 chromosomes, exit
    case 0 | 1 | 2 => false
    // Otherwise execute another reproduction cycle, starting with selection
    case _ =>
      rand.setSeed(rand.nextInt + System.currentTimeMillis)
      population.select(score, configurator.softLimit(cycle)) //1. Selection
      population xOver rand.nextDouble * configurator.getXOverProb //2. Cross-over
      population mutate rand.nextDouble * configurator.getMuProb //3. Mutation
      true
  }
}

/**
 * Companion object for the Reproduction class. This singleton is used
 * to define the default constructor of the Reproduction class.
 * @author Patrick Nicolas
 */
private[ga] object Reproduction {

  /**
   * Default constructor for a reproduction cycle
   * @param score Scoring function of a chromosome (unfitness of the candidate solution)
   */
  def apply[U, T <: Gene[U]](score: Chromosome[U, T] => Unit): Reproduction[U, T] =
    new Reproduction[U, T](score)
}

 */

