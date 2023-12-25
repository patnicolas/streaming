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
 * Define a parameterized gene
 * @param t Object of type T represented by a Gene
 * @param quantizer Quantizer associated with this Gene T <-> Bits sequence
 * @tparam T Type of underlying object for this gene
 *
 * @author Patrick Nicolas
 */
@throws(classOf[GAException])
private[ga] class Gene[T] private (
  t: T,
  quantizer: Quantizer[T],
  override val mutationProbThreshold: Double) extends MutationOp {

  // Encoding as a sequence if bits
  private[this] val bitsSequence: BitsRepr = quantizer(t)

  // Encoding as Bit set
  private[this] val encoded: util.BitSet = {
    val bs =  new java.util.BitSet(quantizer.encodingLength)
    bitsSequence.indices.foreach(index => bs.set(index, bitsSequence(index) == 1))
    bs
  }

  def mutate(): Gene[T] = {
    var newValue: T = null.asInstanceOf[T]
    do {
      val bitsSet = apply(encoded, quantizer.encodingLength)
      val bitsSequence = ga.repr(bitsSet, quantizer.encodingLength)
      newValue = quantizer.unapply(bitsSequence)
    } while(!quantizer.isValid(newValue))

    Gene[T](newValue, quantizer, mutationProbThreshold)
  }



  final def size(): Int = quantizer.encodingLength

  final def getEncoded: util.BitSet = encoded

  final def getBitsSequence: BitsRepr = bitsSequence

  final def getValue: T = t

  def decode(bits: BitsRepr): Gene[T] = {
    val gene = quantizer.unapply(bits)
    new Gene[T](gene, quantizer, mutationProbThreshold)
  }

  def ==(otherGene: Gene[T]): Boolean = otherGene.getBitsSequence == bitsSequence

  @inline
  final def repr: String = bitsSequence.mkString(" ")
  override def toString: String =
    s"${t.toString}: encoding length: ${quantizer.encodingLength}"
}



private[ga] object Gene {

  def apply[T](t: T, quantizer: Quantizer[T], mutationProbThreshold: Double): Gene[T] = new
      Gene[T](t, quantizer, mutationProbThreshold)

  def apply[T](quantizer: Quantizer[T], mutationProbThreshold: Double): Gene[T] =
    new Gene[T](null.asInstanceOf[T], quantizer, mutationProbThreshold)
}