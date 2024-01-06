package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec
import java.util

private[ga] final class MutationOpTest extends AnyFlatSpec{

  it should "Succeed mutating a chromosome as a bit set" in {
    val encodingLength = 5
    val validRange = Seq[Int](6, 8, 10, 12)
    val gene: Gene[Int] = Gene[Int]("id", 8, new GAEncoderInt(encodingLength, validRange))
    val myMutationOp = new MutationOp {
      override val mutationProbThreshold: Double = 0.8
    }

    val mutatedGene = myMutationOp.mutate(gene)
    println(mutatedGene.toString)
  }

  /*
  it should "Succeed mutation chromosome as a list of integers" in {
    import org.pipeline.ga._

    val encodingLength = 5
    val input = List[Int](1, 6, 3, 4, 2, 5)
    implicit val bitsIntEncoder: BitsIntEncoder = new BitsIntEncoder(encodingLength)
    val chromosome: Chromosome[Int] = Chromosome[Int](input, encodingLength)
    val encoded = chromosome.getEncoded
    println(encoded.toString)
    println(chromosome.repr)

    val myMutationOp = new MyMutationOp(0.95)
    val mutatedBitSet: util.BitSet = myMutationOp(encoded, encodingLength, input.length)
    val mutatedChromosome = chromosome.decode(mutatedBitSet, encodingLength)
    println(s"Mutated Chromosome: ${mutatedChromosome.toString}")
  }

  ignore should "Succeed encoding/decoding Chromosome" in {
    val input = Seq[Int](4, 1, 3, 2)
    val encodingLength = 4
    val chromosomeInt = Chromosome[Int](input, encodingLength)
    println(s"Original Chromosome: ${chromosomeInt.toString}")
    val bitSet = chromosomeInt.getEncoded
    println(ga.repr(bitSet, encodingLength))

    val mutatedChromosome = chromosomeInt.decode(bitSet)
    println(s"Not mutated chromosome ${mutatedChromosome.toString}")
  }

  it should "Succeed mutating a sequence of integer" in {
    val input = Seq[Int](4, 1, 3, 2)
    val encodingLength = 4
    val chromosomeInt = Chromosome[Int](input, encodingLength)
    println(s"Original Chromosome: ${chromosomeInt.toString}")
    val bitSet = chromosomeInt.getEncoded
    println(ga.repr(bitSet, encodingLength))

    val myMutationOp = new MyMutationOp(0.8)
    val mutatedBitSet = myMutationOp(bitSet)
    val mutatedChromosome = chromosomeInt.decode(mutatedBitSet)
    println(s"Mutated chromosome: ${mutatedChromosome.toString}")
  }

 */
}


private[ga] object MutationOpTest  {
  class MyMutationOp(override val mutationProbThreshold: Double) extends MutationOp

  def createBitSet(numBits: Int, flag: Boolean): util.BitSet = {
    (0 until numBits).foldLeft(new util.BitSet(numBits))(
      (bs, index) => {
        bs.set(index, flag)
        bs
      }
    )
  }
}
