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

import org.pipeline.ga

import java.util
import scala.util.Random



/**
 * Define a parameterized gene defined as a pair {value, quantizer}
 * @param t Object of type T represented by a Gene
 * @param quantizer Quantizer associated with this Gene T <-> Bits sequence
 * @tparam T Type of underlying object for this gene
 *
 * @author Patrick Nicolas
 */
@throws(classOf[GAException])
private[ga] class Gene[T : Ordering] private (t: T, quantizer: Quantizer[T]) {

  private[this] val rand = new Random(42L)

  // Encoding as a sequence if bits
  private[this] val bitsSequence: BitsRepr = quantizer(t)

  // Encoding as Bit set
  private[this] val encoded: util.BitSet = {
    val bs =  new java.util.BitSet(quantizer.encodingLength)
    bitsSequence.indices.foreach(index => bs.set(index, bitsSequence(index) == 1))
    bs
  }

  /**
   * Mutates this gene using the MutationOp operator
   * @return Mutated gene of same type
   */
  def mutate(mutationOp: MutationOp): Gene[T] = mutationOp(this)

  /**
   * Mutates this gene given a mutation probability
   * @return Mutated gene of same type
   */
  def mutate(mutationProb: Double): Gene[T] = {
    require(
      mutationProb >= 1e-5 && mutationProb < 0.5,
      s"Mutation probability $mutationProb is out of range [1e-5, 0,5]"
    )
    (new MutationOp{
      override val mutationProbThreshold: Double = mutationProb
    })(this)
  }


  /**
   * Convert a bit set into a value of feature type
   * @param bitsSet Bitset
   * @return Feature value of parameterized type
   */
  def getValidValue(bitsSet: util.BitSet): T = {
    val bitsSequence = ga.repr(bitsSet, quantizer.encodingLength)
    quantizer.unapply(bitsSequence)
  }

  final def getQuantizer: Quantizer[T] = quantizer


  final def size(): Int = quantizer.encodingLength

  final def getEncoded: util.BitSet = encoded

  final def getBitsSequence: BitsRepr = bitsSequence

  final def getValue: T = t

  def decode(bits: BitsRepr): Gene[T] = {
    val gene = quantizer.unapply(bits)
    new Gene[T](gene, quantizer)
  }

  def ==(otherGene: Gene[T]): Boolean = otherGene.getBitsSequence == bitsSequence

  @inline
  final def repr: String = bitsSequence.mkString(" ")
  override def toString: String =
    s"${t.toString}: encoding length: ${quantizer.encodingLength}"
}



private[ga] object Gene {

  def apply[T : Ordering](t: T, quantizer: Quantizer[T]): Gene[T] = new Gene[T](t, quantizer)

  /**
   * Generate a random gene for initialization of the population
   * @return Randomly generated Gene
   */
  def apply[T : Ordering](quantizer: Quantizer[T]): Gene[T] =
    new Gene[T](quantizer.rand, quantizer)
}