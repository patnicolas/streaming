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

import org.streaming.ga.Chromosome.CostFactor
import org.streaming.ga.Gene.GeneIndexer

import scala.annotation.implicitNotFound
import scala.collection.mutable
import scala.util.Random


/**
 * Class that implements a parameterized chromosome using an encoding scheme and
 * an objective/fitness function. A chromosome is a container or list of Gene that
 * represents candidate solution to a problem or candidate model to a dataset.
 *
 * @tparam T Parametized type upper bounded by '''Gene'''
 * @constructor Create a chromosome with the parameterized sub-type of Gene
 * @throws IllegalArgumentException if the genetic code is undefined or empty
 * @param code List of Genes or sub types composing this chromosomes.
 * @author Patrick Nicolas
 * @note This particular implementation computes the chromosome cost or unfitness.
 *       The fitness value of a chromosome is computes as 1/cost
 */
@throws(classOf[IllegalArgumentException])
final private[ga] class Chromosome[U, T <: Gene[U]](val code: List[T]){
  require(code.nonEmpty, "Chromosome Cannot create a chromosome from undefined genes")

  private var cost: Double = CostFactor * (1.0 + Random.nextDouble)

  def getCost: Double = cost

  /**
   * Define the cross-over operator to be applied on this chromosome. The cross-over
   * is hierarchical. The algorithm selects the gene associated to the cross-over index,
   * swap all the genes of higher index (below or after the cross-over gene) between
   * the two parents and finally swap the bits within the cross-over gene.
   *
   * The cross over operation generates two off springs from the two original parents.
   * The off-springs are added to the current population along with the parents.
   *
   * @param that other parent chromosome
   * @param geneIndexer Genetic index for the cross-over.
   * @throws IllegalArgumentException if the other chromosome is undefined, or have a
   *                                  different size  or if the cross-over factor is out of range.
   * @return the pair of offspring chromosomes */
  @throws(classOf[IllegalArgumentException])
  def xOver(that: Chromosome[U, T], geneIndexer: GeneIndexer): (Chromosome[U, T], Chromosome[U, T]) = {
    require(!that.isEmpty, "Cannot cross-over chromosome with an undefined parent")
    require(this.size == that.size, s"Chromosomes $size and that ${that.size} have different size")

    // First use the global index (module the number of gene
    val xOverIdx = geneIndexer.geneIndex
    val xGenes = spliceGene(geneIndexer, that.code(xOverIdx))

    // Then recombine the offspring along the cross-over bit
    val offSpring1 = code.slice(0, xOverIdx) ::: xGenes._1 :: that.code.drop(xOverIdx + 1)
    val offSpring2 = that.code.slice(0, xOverIdx) ::: xGenes._2 :: code.drop(xOverIdx + 1)

    // Returns the two offsprings that are added to the current population
    (Chromosome[U, T](offSpring1), Chromosome[U, T](offSpring2))
  }

  /**
   * Mutation operator that flip a gene selected through a mutation index.
   * The mutated gene is added to the population (gene pool).
   *
   * @param geneIndex Genetic index
   * @throws IllegalArgumentException if mutation coefficient, mu is out of range
   * @return A new mutated chromosome */
  def mutate(geneIndex: GeneIndexer): Chromosome[U, T] = {
    // Get the mutation index in the gene to mutate, chOpIdx
    val mutated = code(geneIndex.geneIndex) mutate geneIndex

    // Flip the bit at index 'gIdx.chOpIdx,
    val xs = code.indices.map(
      i => if (i == geneIndex.geneIndex) mutated.asInstanceOf[T] else code(i)
    ).toList
    Chromosome[U, T](xs)
  }

  /**
   * Normalize the fitness of this chromosome with a factor. This
   * operation is required by the selection algorithm.
   *
   * @param normalizeFactor normalization factor
   * @throws IllegalArgumentException if the normalization factor is less than EPS */
  @throws(classOf[IllegalArgumentException])
  def normalize(normalizeFactor: Double): Unit = {
    require(
      Math.abs(normalizeFactor) > Chromosome.Eps,
      s"Cannot normalize with $normalizeFactor > ${Chromosome.Eps}"
    )
    cost /= normalizeFactor
  }

  /**
   * Decode this chromosome by applying a type conversion from Gene to T
   *
   * @param d implicit conversion of Gene to the parameterized type T which is a sub-class of Gene */
  @implicitNotFound(msg = "Chromosome.decode Conversion from Gene to $T is undefined")
  def decode(implicit d: Gene[U] => T): List[T] = code.map(d(_))

  /**
   * Clone this chromosome by duplicating its genes
   *
   * @return Duplicate of this chromosome */
  override def clone: Chromosome[U, T] = Chromosome[U, T](code)

  /**
   * Returns the size of this chromosome as the number of genes it contains,.
   *
   * @return Number of genes
   */
  @inline
  def size: Int = code.size

  @inline
  private def isEmpty: Boolean = code.isEmpty

  private def fitness: Double = if (cost < 1e-10) 1e+10 else 1.0 / cost

  /**
   * Stringize the genetic code of this chromosome
   * @return Genetic code {0, 1} for this chromosome
   */
  override def toString: String = String.valueOf(code.toString())

  /**
   * Symbolic representation of the chromosome as a sequence of symbolic representation
   * of the genes it contains
   * @return sequence of symbolic representation of the genes of this chromosomes
   */
  def symbolic: String = s"${code.map(_.symbolic).mkString(" ")} cost= $cost fitness: $fitness"

  /*
   * Auxiliary method to splice this chromosome with another
   * chromosome with the genetic material thatCode, along the
   * bit of index gIdx
   */
  private def spliceGene(geneIndexer: GeneIndexer, thatCode: T): (T, T) = (
    (this.code(geneIndexer.geneIndex) xOver (thatCode, geneIndexer)).asInstanceOf[T],
    (thatCode xOver (code (geneIndexer.geneIndex), geneIndexer)).asInstanceOf[T]
  )
}


/**
 * Companion object to a Chromosome used to define the constructors
 * @author Patrick Nicolas
 * @since 0.97 September 2, 2013
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 10 Genetic Algorithm/Genetic algorithm components
 */
private[ga] object Chromosome {

  private val Eps = 1e-10
  val CostFactor = 500.0

  /**
   * Default (Generic code) constructor for the Chromosome
   * @param code List of Genes or sub types composing this chromosomes.
   */
  def apply[U, T <: Gene[U]](code: List[T]): Chromosome[U, T] = new Chromosome[U, T](code)

  /**
   * Symbolic constructor for the Chromosome
   * @param predicates List of predicates of type T for this chromosome
   * @param encode Function that convert a predicate to a Gene
   * @throws IllegalArgumentException if either the predicates are undefined (empty)
   * @return Chromosome built from predicates and an encoding function
   */
  @throws(classOf[IllegalArgumentException])
  def apply[U, T <: Gene[U]](predicates: List[T], encode: T => Gene[U]): Chromosome[U, T] = {
    require(predicates.nonEmpty, "Chromosome.apply List of predicates is undefined"
    )

    // Create a chromosome with a single gene for a single predicate
    // or a list of gene for multiple predicate.
    // A gene is actually generated by encoding the predicate
    new Chromosome[U, T](
      if (predicates.size == 1)
        List[T](encode(predicates.head).asInstanceOf[T])
      else
        predicates./:(List[T]())((xs, t) => encode(t).asInstanceOf[T] :: xs)
    )
  }

  /**
   * Type for the pool of chromosomes. A Pool of chromosomes is an arbitrary
   * array of chromosomes.
   */
  type ChomosomesPool[U, T <: Gene[U]] = mutable.ArrayBuffer[Chromosome[U, T]]

  /**
   * Define a Null Chromosome
   */
  def nullChromosome[U, T <: Gene[U]]: Chromosome[U, T] = new Chromosome[U, T](List.empty[T])
}