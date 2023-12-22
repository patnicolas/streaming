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
package org.pipeline.ga

import org.pipeline.ga.Gene.BitsIntEncoder

import java.util
import scala.annotation.tailrec


/**
 *
 * @param t
 * @param encodingLength
 * @param quantizer$T$0
 * @param bitsIntEncoder
 * @tparam T
 */
private[ga] class Gene[T : Quantizer] private (
  t: T,
  encodingLength: Int
)(implicit bitsIntEncoder: BitsIntEncoder) {

  private[this] val bitsSequence: List[Int] = {
    val quantizer = implicitly[Quantizer[T]]
    val value = quantizer(t)
    bitsIntEncoder(value)
  }

  private[this] val encoded: util.BitSet = {
    val bs =  new java.util.BitSet(encodingLength)
    bitsSequence.indices.foreach(index => bs.set(index, bitsSequence(index) == 1))
    bs
  }

  final def size(): Int = encodingLength


  final def getEncoded: util.BitSet = encoded

  final def getBitsSequence: List[Int] = bitsSequence

  def decode(bits: List[Int]): Gene[T] = {
    val quantizer = implicitly[Quantizer[T]]
    val encodedValue = bitsIntEncoder.unapply(bits)
    val gene = quantizer.unapply(encodedValue)
    new Gene[T](gene, encodingLength)
  }

  def ==(otherGene: Gene[T]): Boolean = otherGene.getBitsSequence == bitsSequence

  @inline
  final def repr: String = bitsSequence.mkString(" ")
  override def toString: String =
    s"${t.toString}: encoding length: ${encodingLength}"
}

private[ga] object Gene {

  def apply[T: Quantizer](
    t: T,
    encodingLength: Int
  )(implicit bitsIntEncoder: BitsIntEncoder): Gene[T] = new Gene[T](t, encodingLength)

  def apply[T: Quantizer](encodingLength: Int)(implicit bitsIntEncoder: BitsIntEncoder): Gene[T]
  = new Gene[T](null.asInstanceOf[T], encodingLength)

  class BitsIntEncoder(encodingLength: Int){
    def apply(n: Int): List[Int] = {
      @tailrec def encodeInt(n: Int, bits: List[Int], index: Int): List[Int] = {
        if (index >= encodingLength) bits else {
          val bit = n & 0x01
          encodeInt(n >> 1, bit :: bits, index + 1)
        }
      }

      encodeInt(n, List[Int](), 0)
    }

    def unapply(bits: List[Int]): Int = {
      @tailrec def decodeInt(bits: List[Int], index: Int, value: Int): Int = {
        if (index >= bits.length) value else {
          val newValue = if ((bits(index) & 0x01) == 0x01) value + (1 << index) else value
          decodeInt(bits, index + 1, newValue)
        }
      }

      decodeInt(bits.reverse, 0, 0)
    }
  }
}




/*
import Gene.Encoding
import scala.annotation.implicitNotFound


/**
 * Implementation of a gene as a tuple (value, operator) for example the
 * of the rule IF( _ operator value) THEN action.
 * (i.e. IF (RSI > 0.8 THEN Stock over-bought ). The gene has a fixed size
 * of bits with in this case, two bits allocated to the operator and
 * 32 bits allocated to the value. The floating point value(min, max) is
 * digitized as integer [0, 2&#94;32-1]. The discretization function is provided
 * implicitly. The bits are implemented by the Java BitSet class.
 *
 * @constructor Create a gene instance.
 * @param id     Identifier for the Gene
 * @param target Target or threshold value.It is a floating point value to be digitized
 *               as integer
 * @param op     Symbolic operator associated to this gene
 * @param quantization  implicit discretization function from Floating point value to integer.
 * @author Patrick Nicolas
 */
@implicitNotFound("Gene encoding requires double to integer conversion")
@implicitNotFound("Gene encoding requires quantization")
private[ga] class Gene[U] private (
  val id: String,
  val target: U,
  val op: Operator
)(implicit quantization: Quantization[U], encoding: Encoding){
  import Gene._

  /**
   * Bits encoding of the tuple (value, operator) into bits {0, 1} executed
   * as part of the instantiation of a gene class.
   */
  private lazy val bits = apply(target, op)

  def apply(value: U, operator: Operator): java.util.BitSet = {
    val bitset = new java.util.BitSet(encoding.length) // Encode the operator
    encoding.rOp foreach (i => if (((operator.id >> i) & 0x01) == 0x01) bitset.set(i))

    // Encode the value using the quantization function
    encoding.rValue foreach(i => if (((quantization.toInt(value) >> i) & 0x01) == 0x01) bitset.set(i))
    bitset
  }

  def unapply(bitSet: java.util.BitSet): (U, Operator) = (
    quantization.toU(BitsOperations.convert(encoding.rValue, bits)),
    op(BitsOperations.convert(encoding.rOp, bits))
  )

  /**
   * Create a clone of this gene by duplicating its genetic material (bits).
   * @return identical Gene
   */
  override def clone: Gene[U] = (0 until bits.length).foldLeft(
    Gene(id, target, op))( (enc, n) => {
      if (bits.get(n)) enc.bits.set(n)
      enc
    }
  )

  /**
   * Virtual constructor for classes inherited from Gene. The virtual constructor
   * is used by cloning, mutation and cross-over genetic operators.
   * This method has to be overridden for each Gene sub-class.
   * @param id     Identifier for the gene
   * @param target Target/threshold value associated to the gene
   * @param op     Operator for the input and target value '''input operator target'''
   * @return New instance of the same gene.
   */
  def toGene(id: String, target: U, op: Operator) = new Gene[U](id, target, op)

  /**
   * Generic method to compute the score of this gene. The score of the genes in a
   * chromosome are summed as the score of the chromosome.
   * @return Default score of this gene (-1)
   */
  def score: Double = -1.0

  /**
   * Implements the cross-over operator between this gene and another parent gene.
   * @param geneIndexer Genetic Index for this gene
   * @param that    other gene used in the cross-over
   * @return A single Gene as cross-over of two parents.
   */
  def xOver(that: Gene[U], geneIndexer: GeneIndexer): Gene[U] = {
    val clonedBits = BitsOperations.duplicate(bits)

    Range(geneIndexer.geneIndex, bits.size).foreach(
      n => if (that.bits.get(n)) clonedBits.set(n) else clonedBits.clear(n)
    )
    val (target, operator) = unapply(clonedBits)
    new Gene(id, target, operator)
  }


  /**
   * Implements the mutation operator on this gene
   * @param geneIndexer bits indexer within the gene the cross-over and mutation operation
   * @return A mutated gene
   */
  def mutate(geneIndexer: GeneIndexer): Gene[U] = {
    val clonedBits = BitsOperations.duplicate(bits) // flip the bit
    clonedBits.flip(geneIndexer.geneIndex)
    // Decode or convert the bit set into a symbolic representation for the gene
    val valOp = unapply(clonedBits)
    new Gene(id, valOp._1, valOp._2)
  }

  /**
   * Textual description of the symbolic representation of this gene
   * @return description of gene id, operator and target value
   */
  @inline def symbolic: String = s"$id ${op.toString} $target"

  /**
   * Textual description of the genetic representation of this gene
   */
  override def toString: String =
    (0 until bits.size).map(n => if (bits.get(n)) "1" else "0").mkString("")
}

/**
 * Companion object for the Gene class to define constants, its constructors
 * and duplication of genetic code.
 * @author Patrick Nicolas
 */
private[ga] object Gene{

  /**
   * Default constructor for a Gene
   * @param id       Identifier for the Gene
   * @param target   Target or threshold value.It is a floating point value to be digitized as integer
   * @param op       Symbolic operator associated to this gene
   * @param quantization    implicit quantization function from Floating point value to integer.
   * @param encoding implicit encoding function for the gene
   */
  def apply[U](
    id: String,
    target: U,
    op: Operator)(implicit quantization: Quantization[U], encoding: Encoding): Gene[U] =
    new Gene[U](id, target, op)

  class Encoding(nValueBits: Int, nOperatorBits: Int){
    val rValue: Seq[Int] = Range(0, nValueBits)
    val length: Int = nValueBits + nOperatorBits
    val rOp: Seq[Int] = Range(nValueBits, length)
  }


  /**
   * Class that defines an indexer for Gene (index in the genetic code,
   * an operator such as mutation or cross-over acts upon (hierarchical address)
   * @param geneIndex Index of the gene in the chromosome, manipulated by a genetic operator
   * @param bitsIndex Index of the bit(s) in the gene that is manipulated by a genetic operator.
   * @author Patrick Nicolas
   */
  case class GeneIndexer(geneIndex: Int, bitsIndex: Int){
    override def toString: String = s"ch index: $geneIndex gene index: $bitsIndex"
  }


  object BitsOperations{
    final private val VALUE_SIZE = 32
    final private val OP_SIZE = 2
    var defaultEncoding = new Encoding(VALUE_SIZE, OP_SIZE)

    /**
     * Clone the genetic code of this gene
     * @param bits Bitset of this gene
     * @return duplicated genetic code
     */
    def duplicate(bits: java.util.BitSet): java.util.BitSet =
      Range(0, bits.length).foldLeft(new java.util.BitSet)(
        (enc, n) => {
          if (bits.get(n)) enc.set(n)
          enc
        }
    )

    /**
     * Convert a range of bits within a bit into an integer
     */
    def convert(bitsRangeIndices: Seq[Int], bits: java.util.BitSet): Int =
      bitsRangeIndices.foldLeft(0)((v, i) => v + (if (bits.get(i)) 1 << i else 0))
  }
}

 */
