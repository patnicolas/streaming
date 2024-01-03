package org.pipeline.ga


import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class GeneTest extends AnyFlatSpec{

  it should "Succeed instantiating a gene with integer type" in {
    val validRange = Seq[Int](6, 8, 10, 12)
    val gaEncoder = new GAEncoderInt(4, validRange)
    val input = 5
    val gene = Gene[Int]("id", input, gaEncoder)
    println(gene.toString)
    println(gene.getEncoded)
    println(gene.getBitsSequence)
  }

  it should "Succeed instantiating a gene with floating point type" in {
    var condition = false
    try {
      val validRange = Seq[Float](6.0F, 8.0F, 10.0F, 12.0F)
      val gaEncoder = new GAEncoderFloat(6, scaleFactor = 1.0F, validRange)
      val input = 2.0F
      val gene = Gene[Float]("id", input, gaEncoder)
      println(gene.toString)
      println(gene.getEncoded)
      println(gene.getBitsSequence)
      condition = true
    } catch {
      case e: GAException =>
        condition = false
    }
    finally {
      assert(condition)
    }
  }

  it should "Succeed instantiating a gene with outbound floating point type" in {
    var condition = false
    try {
      val validRange = Seq[Float](6.0F, 8.0F, 10.0F, 12.0F)
      val gaEncoder = new GAEncoderFloat(encodingLength = 6, scaleFactor = 1.0F, validRange)
      val input = 18.0F
      val gene = Gene[Float]("id", input, gaEncoder)
      println(gene.toString)
      println(gene.getEncoded)
      println(gene.getBitsSequence)
    }
    catch {
      case e: GAException => condition = true
    } finally {
      assert(condition)
    }
  }

  it should "Succeed decoding a gene" in {
    val validRange = Seq[Int](6, 8, 10, 12)
    val gaEncoder = new GAEncoderInt(4, validRange)
    val input = 5
    val gene = Gene[Int]("id", input, gaEncoder)
    val bitsSequence = gene.getBitsSequence
    val decodedGene = gene.decode(bitsSequence)
    assert(gene == decodedGene)
  }

  it should "Succeed mutating a gene as floating point" in {

    try {
      val validRange = Seq[Float](6.0F, 8.0F, 10.0F, 12.0F)

      val gaEncoder = new GAEncoderFloat(6, scaleFactor = 2.0F, validRange)
      val input = 9.0F
      val gene = Gene[Float]("id", input, gaEncoder)
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
