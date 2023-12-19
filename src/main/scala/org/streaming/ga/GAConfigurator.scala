/**
 * Copyright 2022,2023 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.streaming.ga


/**
 * Configuration class that defines the key parameters for the execution of the
 * genetic algorithm solver (or optimizer). The list of configuration parameters include
 * mutation, cross-over ratio, maximum number of optimization cycles (or epochs) and a soft limiting
 * function to constraint the maximum size of the population of chromosomes at each cycles
 * (or epochs)
 *
 * {{{
 *   soft limit function
 *   max_population_size (t) = max_population_size (t-1) *softLimit(t)
 * }}}
 * @constructor Create a configuration for the genetic algorithm.
 * @throws IllegalArgumentException if some of the parameters are out of bounds such
 *                                  as maxPopulationSize > 1 or rejection rate < 0.95
 * @param xOverProb Value of the cross-over parameter, in the range [0.0, 1.0] used to compute
 *              the index of bit string representation of the chromosome for cross-over
 * @param muProb Value in the range [0.0, 1.0] used to compute the index of the bit or
 *           individual to be mutate in each chromosome.
 * @param maxCycles Maximum number of iterations allowed by the genetic solver
 *                  (reproduction cycles).
 * @param softLimit  Soft limit function (Linear, Square or Boltzman) used to attenuate
 *                   the impact of cross-over or mutation operation during optimization
 *
 * @author Patrick Nicolas
 */
@throws(classOf[IllegalArgumentException])
private[ga] final class GAConfigurator private (
  val xOverProb: Double,
  val muProb: Double,
  val maxCycles: Int,
  val softLimit: Int => Double
) {
  import GAConfigurator._

  check(xOverProb, muProb, maxCycles)


  @inline
  def getXOverProb: Double = xOverProb

  @inline
  def getMuProb: Double = muProb

  /**
   * re-compute the mutation factor using an attenuator
   * @return soft limit computed for this cycle
   */
    /*
  @throws(classOf[IllegalArgumentException])
  val mutation = (cycle: Int) => {
    require(cycle >= 0 && cycle < maxCycles, s"GAConfig Iteration $cycle is out of range")
    softLimit(cycle)
  }

     */

  /**
   * Textual representation of the configuration object
   */
  override def toString: String = s"Cross-over: $xOverProb Mutation: $muProb"
}

/**
 * Singleton that define the attenuator function for computing the cross-over or
 * mutation index of chromosomes, according the number of iterations in the genetic
 * algorithm optimization.
 *
 * @author Patrick Nicolas
 */
private[ga] object GAConfigurator {

  /**
   * Default constructor for the GAConfig class
   * @param xOverProb Value of the cross-over parameter, in the range [0.0, 1.0] used to compute
   *              the index of bit string representation of the chromosome for cross-over
   * @param muProb Value in the range [0.0, 1.0] used to compute the index of the bit or
   *           individual to be mutate in each chromosome.
   * @param maxCycles Maximum number of iterations allowed by the genetic solver
   *                  (reproduction cycles).
   * @param softLimit  Soft limit function (Linear, Square or Boltzmann) used to attenuate
   *                   the impact of cross-over or mutation operation during optimization.</span></pre>
   */
  def apply(
    xOverProb: Double,
    muProb: Double,
    maxCycles: Int,
    softLimit: Int => Double): GAConfigurator =
    new GAConfigurator(xOverProb, muProb, maxCycles, softLimit)

  private val DefaultSoftLimit = (n: Int) => -0.01 * n + 1.001

  /**
   * Constructor for the GAConfig class with a default soft limit defined as
   * {{{
   *  f(n) = 1.001 -0.01.n.
   *  }}}
   * @param xOverProb Value of the cross-over parameter, in the range [0.0, 1.0] used to
   *              compute the index of bit string representation of the chromosome for cross-over
   * @param muProb Value in the range [0.0, 1.0] used to compute the index of the bit or
   *           individual to be mutate in each chromosome.
   * @param maxCycles Maximum number of iterations allowed by the genetic solver
   *                  (reproduction cycles).
   */
  def apply(xOverProb: Double, muProb: Double, maxCycles: Int): GAConfigurator =
    new GAConfigurator(xOverProb, muProb, maxCycles, DefaultSoftLimit)

  private val DEFAULT_MAX_CYCLES = 2048
  private def check(xOverProb: Double, muProb: Double, maxCycles: Int): Unit = {
    require(
      maxCycles > 5 & maxCycles < DEFAULT_MAX_CYCLES,
      s"Maximum number of iterations $maxCycles is out of bounds [0, MAX_CYCLES]"
    )
    require(muProb > 0.0 && muProb < 1.0, s"Mutation factor $muProb is out of bounds [0, 1]")
    require(xOverProb > 0.0 && xOverProb < 1.0, s"Crossover factor $xOverProb is out of bounds [0, 1]")
  }
}
