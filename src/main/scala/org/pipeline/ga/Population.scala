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
/*
package org.pipeline.ga

import org.pipeline.ga.Gene.GeneIndexer
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


/**
 * Class that defines a population of chromosomes. A population is initialized and evolves
 * through multiple reproduction cycles. The size of the population varies
 * over time following successive, iterative selection but is bounded by an upper limit to
 * avoid a potential explosion of the number chromosomes.
 *
 * @constructor Create a population of chromosome. [chromosomes] Current pool of chromosomes
 * @param limit       Maximum number of chromosomes allowed in this population
 *                    (constrained optimization)
 * @param chromosomes Current pool of chromosomes (type: ArrayBuffer{Chromosome[T])
 * @author Patrick Nicolas
 */
private[ga] class Population[U, T <: Gene[U]] private (
  limit: Int,
  chromosomes: ChomosomesPool[U, T]){
  import Population._

  check(limit, chromosomes)
  private[this] var marker: Double = 0.0

  private def getChromosomes: ChomosomesPool[U, T] = chromosomes

  /**
   * Add an array of chromosomes (or new population) to this existing population and return
   * a new combined population. The new chromosomes are appended to the existing pool
   *
   * @param that New population to be added to the existing population
   * @throws IllegalArgumentException if the population is undefined
   * @return The combined population if the new population is not empty, this population otherwise */
  def +(that: Population[U, T]): Population[U, T] = {
    require(!that.isEmpty, "Cannot add an undefined list of chromosomes")

    if (that.size > 0) Population[U, T](limit, chromosomes ++: that.getChromosomes)
    else this
  }

  /**
   * Add a new Chromosome to this population using a list of genes.
   *
   * @param newCode Genetic code (List of genes) for the new chromosome added to this population
   * @throws IllegalArgumentException if the newCode is either undefined or has an incorrect size. */
  protected def +=(newCode: List[T]): Unit = {
    require(newCode.nonEmpty, "Cannot add an undefined chromosome")

    chromosomes += new Chromosome[U, T](newCode)
  }

  /**
   * Selection operator for the chromosomes pool The selection relies on the
   * normalized cumulative unfitness for each of the chromosome ranked by decreasing
   * order.
   *
   * @param score  Scoring function applied to all the chromosomes of this population
   * @param cutOff Normalized threshold value for the selection of the fittest chromosomes
   * @throws IllegalArgumentException if the cutoff is out of bounds */
  @throws(classOf[IllegalArgumentException])
  def select(score: Chromosome[U, T] => Unit, cutOff: Double): Unit = {
    require(
      cutOff > 0.0 && cutOff < 1.01,
      s"Population.select Cannot select with a cutoff $cutOff out of range"
    )

    // Compute the cumulative score for the entire population
    val cumulative = chromosomes.foldLeft(0.0)((s, c) => s + c.getCost) / ScalingFactor
    marker = cumulative / chromosomes.size

    // Normalize each chromosome unfitness value
    chromosomes foreach (_ normalize cumulative)

    // Sorts the chromosome by the increasing value of their unfitness
    val newChromosomes = chromosomes.sortBy(_.getCost)

    // Apply a cutoff value to the current size of the population
    // if the cutoff has been defined.
    val cutOffSize: Int = (cutOff * newChromosomes.size).floor.toInt
    val newPopSize = if (limit < cutOffSize) limit else cutOffSize

    chromosomes.clear()
    chromosomes ++= newChromosomes.take(newPopSize)
  }

  /**
   * Return the size of the genes that compose the chromosomes of this population.
   * It is assumed that the genes in the chromosomes have identical size.
   *
   * @return number of bits in the gene that compose the chromosomes of this population if
   *         the population is not empty, -1 otherwise
   */
  // final def geneSize: Int = if (chromosomes.nonEmpty) chromosomes.head.code.head.size else -1

  /**
   * Return the number of genes in the chromosomes of this population.
   *
   * @return Number of genes in each of the chromosomes of this population if the population
   *         is not empty, -1 otherwise */
  private final def chromosomeSize: Int = if (chromosomes.nonEmpty) chromosomes.head.size else -1

  /**
   * Applies the cross-over operator on the population by pairing
   * the half most fit chromosomes with the half least fit chromosomes.
   *
   * @param xOverProb cross-over factor [0, 1]
   * @throws IllegalArgumentException if xOver is out of range. */
  @throws(classOf[IllegalArgumentException])
  def xOver(xOverProb: Double): Unit = {
    require(
      xOverProb > 0.0 && xOverProb < 1.0,
      s"Population.+- Cross-over factor $xOverProb on the population is out of range"
    )

    // It makes sense to cross over all the chromosomes in this
    // population if there are more than one chromosome
    if (size > 1) {
      // Breakdown the sorted list of chromosomes into two segments
      val mid = size >> 1
      val bottom = chromosomes.slice(
        mid,
        size
      )

      // Pair a chromosome for one segment with a chromosome
      // from the other segment.Then add those offsprings to the
      // current population
      val geneIndexer = createGeneIndexer(xOverProb)
      val offSprings = chromosomes.take(mid).zip(bottom).map { case (t, b) => t xOver(b, geneIndexer) }
        .unzip
      chromosomes ++= offSprings._1 ++ offSprings._2
    }
  }

  /**
   * Apply the mutation of the population. The operation produces a duplicate set of
   * chromosomes that are mutated using the mutate operator &#94; on chromosome.
   *
   * @param mu mutation factor
   * @return Population with original chromosomes and mutated counter-part
   * @throws IllegalArgumentException if the mutation ratio or coef. mu is out of range [0, 1] */
  @throws(classOf[IllegalArgumentException])
  def mutate(mu: Double): Unit = {
    require(
      mu > 0.0 && mu < 1.0,
      s"Population.^ Mutation factor $mu on the population is out of range"
    )
    chromosomes ++= chromosomes.map(_ mutate createGeneIndexer(mu))
  }

  /**
   * Compute the difference between the N fittest chromosomes of two populations.
   *
   * @param that  The population to be compared to
   * @param depth Number of fittest chromosomes used in the comparison. If the depth exceeds
   *              the size the entire population is used in the comparison
   * @return The depth fittest chromosomes if there are common to both population, None otherwise
   * @throws IllegalArgumentException if mu is out of range [0, 1]
   */
  @throws(classOf[IllegalArgumentException])
  final def diff(that: Population[U, T], depth: Int): Option[ChomosomesPool[U, T]] = {
    require(that.size > 1, "Population.diff Other population has no chromosome")
    require(depth > 0, s"Population.diff depth $depth should be >1")

    // Define the number of chromosomes participating
    // to the comparison of two populations 'this' and 'that'
    val fittestPoolSize = {
      if (depth >= size || depth >= that.size) if (size < that.size) size else that.size
      depth
    } // Deals with nested options. Get the 'depth' most fit
    // chromosomes for this population and 'depth' most fit
    // chromosomes for that population, then compare..
    for {
      first <- fittest(fittestPoolSize)
      second <- that.fittest(fittestPoolSize)
      if !(first.zip(second).exists) { case (x1, x2) => x1 != x2 }
    } yield first
  }

  /**
   * Retrieve the N fittest chromosomes from this population.
   *
   * @param depth Number of fittest chromosomes to retrieve
   * @return The depth fittest chromosomes if the population is not empty, None otherwise
   * @throws IllegalArgumentException If depth is not greater than 0 */
  @throws(classOf[IllegalArgumentException])
  private final def fittest(depth: Int): Option[ChomosomesPool[U, T]] = {
    require(depth > 0, s"Population.fittest Incorrect number of chromosomes: $depth should be >0")
    if (size > 1) Some(chromosomes.take(if (depth > size) size else depth)) else None
  }

  /**
   * Retrieve the fittest chromosome.
   * @return Fittest chromosome if the population is not empty, None otherwise
   * */
  final def fittest: Option[Chromosome[U, T]] =
    if (size > 0) Some(chromosomes.head) else None

  /**
   * Compute the average score or fitness of the current population
   *
   * @return sum of the score of all the chromosomes divided by the number of chromosomes. */
  final def averageCost: Double = marker


  /**
   * Retrieve the number of chromosomes in the population
   *
   * @return Number of chromosomes in this population */
  final def size: Int = chromosomes.size

  final def isEmpty: Boolean = chromosomes.isEmpty

  /**
   * Textual description of the genetic code of this population
   *
   * @return Genetic code for all the chromosomes of this population */
  override def toString: String = chromosomes.map(_.toString).mkString("\n")

  /**
   * Symbolic representation of this population
   *
   * @return Symbolic representation all the chromosomes of this population */
  final def symbolic(): String = chromosomes.map(_.symbolic).mkString("\n")

  /*
     * Compute the genetic index for cross-over and mutation
     * according to a probability value
     * @param prob probability value [0, 1]
     */
  private def createGeneIndexer(prob: Double): GeneIndexer = {
    var idx = (prob * chromosomeSize).floor.toInt
    val geneIndex = if (idx == chromosomeSize) chromosomeSize - 1 else idx

    idx = (prob * size).floor.toInt

    val bitsIndex = if (idx == size) size - 1 else idx
    GeneIndexer(geneIndex, bitsIndex)
  }
}

/**
 * Companion object for the Population class that defines its constructor
 * and validate its parameters.
 *
 * @author Patrick Nicolas
 * @since August 25, 2013
 * @note Scala for Machine Learning Chapter 10 Genetic Algorithm/Genetic algorithm components */
private[ga] object Population{
  private final val ScalingFactor = 100

  /**
   * Default constructor for the population of chromosomes
   * @param limit       Maximum number of chromosomes allowed in this population (constrained
   *                    optimization)
   * @param chromosomesPool Current pool of chromosomes (type: ArrayBuffer{Chromosome[T]) */
  def apply[U, T <: Gene[U]](limit: Int, chromosomesPool: ChomosomesPool[U, T]): Population[U, T] =
    new Population[U, T](limit, chromosomesPool)

  /**
   * Default constructor for the population of chromosomes
   *
   * @param limit       Maximum number of chromosomes allowed in this population (constrained
   *                    optimization)
   * @param chromosomes New list of chromosomes added to the existing pool */
  def apply[U, T <: Gene[U]](limit: Int, chromosomes: List[Chromosome[U, T]]): Population[U, T] =
    new Population[U, T](limit, mutable.ArrayBuffer[Chromosome[U, T]]() ++ chromosomes)

  private val MAX_NUM_CHROMOSOMES = 10000

  private def check[U, T <: Gene[U]](limit: Int, chromosomes: ChomosomesPool[U, T]): Unit = {
    require(chromosomes.nonEmpty, "Population.check: Undefined initial set of chromosomes")
    require(
      chromosomes.nonEmpty && chromosomes.size < limit,
      s"Population.check: The pool of chromosomes ${chromosomes.size} is out of range"
    )
    require(
      limit > 1 && limit < MAX_NUM_CHROMOSOMES,
      s"Maximum number of allowed chromosomes $limit is out of range"
    )
  }

  /**
   * Define a Null population for error checking purpose */
  def nullPopulation[U, T <: Gene[U]]: Population[U, T] =
    new Population[U, T](-1, ArrayBuffer.empty)
}

 */
