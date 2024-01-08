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

import org.pipeline.ga.Reproduction.defaultMaxNumIterations
import org.pipeline.ga.XOverOp.randomStrategy
import org.pipeline.streams.spark.SparkConfiguration

import scala.annotation.tailrec


/**
 *  Define the replication cycle in the execution of the genetic algorithm optimizer.
 *  A replication cycle consists of a selection of chromosomes according to their
 *  fitness/unfitness values, cross-over of pair of chromosomes and mutation
 *
 *  @constructor Create a reproduction cycle for the genetic algorithm.
 *  @tparam T type of gene (inherited from '''Gene''')
 *  @author Patrick Nicolas
 */

/**
 * Define the replication cycle in the execution of the genetic algorithm optimizer.
 * A replication cycle consists of a selection of chromosomes according to their
 * fitness/unfitness values, cross-over of pair of chromosomes and mutation
 * @param execSparkSubmit Function that execute a Spark Submit
 * @param latencyFactor Latency Factor used in the computation of the fitness of chromosomes
 * @param serverHourlyCost Hourly cost of average server or container used in the computation of
 *                         the fitness of chromosomes
 * @param maxPopulationSize Maximum size allowed for the number of chromosomes across the
 *                          reproduction cycle
 * @param xOverProbThreshold Threshold value for the probability to trigger a cross-over
 * @param mutationProbThreshold Threshold value for the probability to trigger a mutation
 * @param xOverStrategy Cross-over strategy
 * @param stopCondition Function or condition used to terminate the execution of genetic algorithm
 */
final private[ga] class Reproduction protected (
  val execSparkSubmit: SparkConfiguration => (Int, Long),
  override val latencyFactor: Float,
  override val serverHourlyCost: Float,
  override val maxPopulationSize: Int,
  override val xOverProbThreshold: Double,
  override val mutationProbThreshold: Double,
  xOverStrategy: String,
  stopCondition: Seq[Chromosome[Int, Float]] => Boolean
) extends ScoreOp with SelectionOp with XOverOp with MutationOp {


  /**
   * Execute the reproduction cycle, implemented as a tail recursion performed after the
   * initial random initialization of chromosomes
   * @param idsInt List if identifiers for the configuration parameters of integer type
   * @param gaEncoderInt Encoders for the genes of type Integer
   * @param idsFloat List if identifiers for the configuration parameters of integer Float
   * @param gaEncoderFloat Encoders for the genes of type Float
   * @return The sequence of chromosomes ranked by decreasing order of their fitness
   */
  def mate(
    idsInt: Seq[String],
    gaEncoderInt: Seq[GAEncoder[Int]],
    idsFloat: Seq[String],
    gaEncoderFloat: Seq[GAEncoder[Float]]): Seq[Chromosome[Int, Float]] = {
    // Initialization of chromosomes
    val initialChromosomes = Seq.fill(maxPopulationSize)(
      Chromosome.rand(idsInt, gaEncoderInt, idsFloat, gaEncoderFloat)
    )
    // Recursive reproduction cycle
    mate(initialChromosomes, iterationCount = 0)
  }

  override def toString: String = {
    s"""Latency scale factor:     $latencyFactor
       |Server hourly rate:       $serverHourlyCost
       |maximum population size:  $maxPopulationSize
       |Threshold crossover prob: $xOverProbThreshold
       |Threshold mutation prob:  $mutationProbThreshold
       |Cross-over strategy:      $xOverStrategy""".stripMargin
  }

  @tailrec
  private def mate(
    chromosomes: Seq[Chromosome[Int, Float]],
    iterationCount: Int,
    maxNumIterations: Int = defaultMaxNumIterations): Seq[Chromosome[Int, Float]] = {
    val offSprings = xOver(chromosomes, xOverStrategy)
    val mutatedChromosomes = mutate(chromosomes ++ offSprings)
    val scoredChromosomes = score(mutatedChromosomes)
    val selectedChromosomes = select(scoredChromosomes)
    // If condition met, exit
    if (iterationCount > defaultMaxNumIterations || stopCondition(selectedChromosomes)) selectedChromosomes
     // Otherwise recurse
    else mate(selectedChromosomes, iterationCount + 1)
  }
}


private[ga] object Reproduction {

  final val defaultMaxNumIterations = 16

  def apply(
    execSparkSubmit: SparkConfiguration => (Int, Long),
    latencyFactor: Float,
    serverHourlyCost: Float,
    maxPopulationSize: Int,
    xOverProbThreshold: Double,
    mutationProbThreshold: Double,
    xOverStrategy: String
  ): Reproduction =  new Reproduction(
    execSparkSubmit,
    latencyFactor,
    serverHourlyCost,
    maxPopulationSize,
    xOverProbThreshold,
    mutationProbThreshold,
    xOverStrategy,
    stopAveCondition)


  def apply(
    execSparkSubmit: SparkConfiguration => (Int, Long),
    latencyFactor: Float,
    serverHourlyCost: Float,
    maxPopulationSize: Int): Reproduction = new Reproduction(
    execSparkSubmit,
    latencyFactor,
    serverHourlyCost,
    maxPopulationSize,
    xOverProbThreshold = 0.2F,
    mutationProbThreshold = 0.01F,
    randomStrategy,
    stopAveCondition)

  /**
   * Stop conditions as the fitness for the best chromosome (Spark Configuration)
   * is at least 20% better than the fitness of the next best chromosome
   */
  final val stopDifferentialCondition = (chromosomes: Seq[Chromosome[Int, Float]]) =>
    if(chromosomes.size == 1) true
    else
      chromosomes.head.fitness/chromosomes(1).fitness >= 1.20

  /**
   * Stop condition as the fitness for the best chromosome is at least 50% better that
   * the average fitness of all other chromosomes.
   */
  final val stopAveCondition = (chromosomes: Seq[Chromosome[Int, Float]]) =>
    if (chromosomes.size ==1) true
    else {
      val chromosomesAveFitness = chromosomes.map(_.fitness).sum/chromosomes.size
      chromosomes.head.fitness / chromosomesAveFitness > 2.0
    }

}


