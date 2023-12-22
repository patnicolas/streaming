package org.pipeline.ga

import org.pipeline.ga.Gene.BitsIntEncoder
import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class GeneTest extends AnyFlatSpec{

  it should "Succeed instantiating a gene" in {
    implicit val bitEncoderInt: BitsIntEncoder = new BitsIntEncoder(4)
    val element = 6
    val gene = Gene[Int](element, 4)
    println(gene.toString)
    println(gene.getEncoded)
    println(gene.getBitsSequence)
  }

  it should "Succeed decoding a gene" in {
    implicit val bitEncoderInt: BitsIntEncoder = new BitsIntEncoder(4)
    val element = 6
    val gene = Gene[Int](element, 4)
    val bitsSequence = gene.getBitsSequence
    val newGene = gene.decode(bitsSequence)
    assert(newGene == gene)
  }
}
