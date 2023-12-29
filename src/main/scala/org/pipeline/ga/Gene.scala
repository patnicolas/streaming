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


/**
 * Define a parameterized gene defined as a pair {value, quantizer}
 * @param t Object of type T represented by a Gene
 * @param quantizer Quantizer associated with this Gene T <-> Bits sequence
 * @tparam T Type of underlying object for this gene
 *
 * @author Patrick Nicolas
 */
@throws(classOf[GAException])
private[ga] class Gene[T] private (t: T, quantizer: Quantizer[T]) {


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


  def getValidValue(bitsSet: util.BitSet): T = {
    var newValue: T = null.asInstanceOf[T]
    do {
      val bitsSequence = ga.repr(bitsSet, quantizer.encodingLength)
      newValue = quantizer.unapply(bitsSequence)
    } while(!quantizer.isValid(newValue))
    newValue
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

  def apply[T](t: T, quantizer: Quantizer[T]): Gene[T] = new Gene[T](t, quantizer)

  def apply[T](quantizer: Quantizer[T]): Gene[T] = new Gene[T](null.asInstanceOf[T], quantizer)
}