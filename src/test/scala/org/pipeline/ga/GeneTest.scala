package org.pipeline.ga


import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class GeneTest extends AnyFlatSpec{

  it should "Succeed instantiating a gene with integer type" in {
    val maxValue = 6
    val quantizer = new QuantizerInt(4, (n: Int) => n <= maxValue)
    val input = 5
    val gene = Gene[Int](input, quantizer, 0.8)
    println(gene.toString)
    println(gene.getEncoded)
    println(gene.getBitsSequence)
  }

  it should "Succeed instantiating a gene with floating point type" in {
    val maxValue = 10.0
    try {
      val quantizer = new QuantizerDouble(6, (x: Double) => x <= maxValue, 2.0)
      val input = 2.0
      val gene = Gene[Double](input, quantizer, 0.9)
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
      val quantizer = new QuantizerDouble(encodingLength = 6, (x: Double) => x <= maxValue, scaleFactor = 10.0)
      val input = 18.0
      val gene = Gene[Double](input, quantizer, mutationProbThreshold = 0.9)
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
    val quantizer = new QuantizerInt(4, (n: Int) => n <= maxValue)
    val input = 5
    val gene = Gene[Int](input, quantizer, 0.8)
    val bitsSequence = gene.getBitsSequence
    val decodedGene = gene.decode(bitsSequence)
    assert(gene == decodedGene)
  }

  it should "Succeed mutating a gene as floating point" in {
    val maxValue = 10.0
    try {
      val quantizer = new QuantizerDouble(6, (x: Double) => x <= maxValue, 2.0)
      val input = 9.0
      val gene = Gene[Double](input, quantizer, 0.9)
      val mutatedGene = gene.mutate()
      println(s"Gene ${gene.toString}\nMutated ${mutatedGene.toString}")
    } catch {
      case e: GAException =>
        val condition = false
        assert(condition)
    }
  }

}
