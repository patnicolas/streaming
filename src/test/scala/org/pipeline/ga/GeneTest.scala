package org.pipeline.ga


import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class GeneTest extends AnyFlatSpec{

  it should "Succeed instantiating a gene with integer type" in {
    val maxValue = 6
    val quantizer = new QuantizerInt(4, maxValue)
    val input = 5
    val gene = Gene[Int](input, quantizer)
    println(gene.toString)
    println(gene.getEncoded)
    println(gene.getBitsSequence)
  }

  it should "Succeed instantiating a gene with floating point type" in {
    val maxValue = 10.0
    try {
      val quantizer = new QuantizerDouble(6, scaleFactor = 1.0, maxValue)
      val input = 2.0
      val gene = Gene[Double](input, quantizer)
      println(gene.toString)
      println(gene.getEncoded)
      println(gene.getBitsSequence)
      assert(true)
    } catch {
      case e: GAException => assert(false)
    }
  }

  it should "Succeed instantiating a gene with outbound floating point type" in {
    val maxValue = 10.0
    try {
      val quantizer = new QuantizerDouble(encodingLength = 6, scaleFactor = 1.0, maxValue = 10.0)
      val input = 18.0
      val gene = Gene[Double](input, quantizer)
      println(gene.toString)
      println(gene.getEncoded)
      println(gene.getBitsSequence)
      assert(false)
    }
    catch {
      case e: GAException => assert(true)
    }
  }

  it should "Succeed decoding a gene" in {
    val maxValue = 6
    val quantizer = new QuantizerInt(4, maxValue)
    val input = 5
    val gene = Gene[Int](input, quantizer)
    val bitsSequence = gene.getBitsSequence
    val decodedGene = gene.decode(bitsSequence)
    assert(gene == decodedGene)
  }

  it should "Succeed mutating a gene as floating point" in {
    val maxValue = 10.0
    try {
      val quantizer = new QuantizerDouble(6, scaleFactor = 2.0, maxValue)
      val input = 9.0
      val gene = Gene[Double](input, quantizer)
      val mutationOp = new MutationOp {
        val mutationProbThreshold: Double = 0.8
      }
      val mutatedGene = gene.mutate(mutationOp)
      println(s"Gene ${gene.toString}\nMutated ${mutatedGene.toString}")
    } catch {
      case e: GAException =>
        val condition = false
        assert(condition)
    }
  }

}
