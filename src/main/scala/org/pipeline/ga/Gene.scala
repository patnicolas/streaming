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

import java.util
import scala.annotation.tailrec
import org.pipeline.ga.Quantizer


/**
 *
 * @param t
 * @param encodingLength
 * @param quantizer
 * @tparam T
 */
@throws(classOf[GAException])
private[ga] class Gene[T] private (t: T, quantizer: Quantizer[T]) {

  private[this] val bitsSequence: BitsRepr = quantizer(t)

  private[this] val encoded: util.BitSet = {
    val bs =  new java.util.BitSet(quantizer.encodingLength)
    bitsSequence.indices.foreach(index => bs.set(index, bitsSequence(index) == 1))
    bs
  }

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