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
 * Define the scoring operator for a population of chromosomes each associated with a given
 * Spark configuration.
 * Practically, each Spark configuration is loaded prior the execution of a Spark submit.
 * @author Patrick Nicolas
 */
trait ScoreOp{
self =>
  protected[this] val execSparkSubmit: SparkConfiguration => (Int, Long)
  protected[this] val latencyFactor: Float
  protected[this] val serverHourlyCost: Float

  /**
   * Load a set of Spark configurations (each configuration defined as a set of parameters)
   * then trigger Spark submit for each configuration.
   * The execution of various Spark configuration is done concurrently through distributed
   * containers
   * @param population Set of chromosomes representing Spark configurations
   * @return Chromosomes with updated fitness values
   */
  def apply(population: Seq[Chromosome[Int, Float]]): Seq[Chromosome[Int, Float]] =
    population.map(ch => score(ch))

  private def score(chromosome: Chromosome[Int, Float]): Chromosome[Int, Float] = {
    val sparkConfiguration = ConfigEncoderDecoder.decode(chromosome)
    val (numServers, latency) = execSparkSubmit(sparkConfiguration)
    chromosome.fitness = 1.0/(math.exp(latencyFactor*latency) + serverHourlyCost*numServers)
    chromosome
  }
}
