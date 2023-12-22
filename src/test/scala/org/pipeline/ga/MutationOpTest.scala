package org.pipeline.ga

import org.pipeline.ga
import org.pipeline.ga.MutationOpTest.{MyMutationOp, createBitSet}
import org.scalatest.flatspec.AnyFlatSpec

import java.util

private[ga] final class MutationOpTest extends AnyFlatSpec{

  ignore should "Succeed mutating a chromosome" in {
    val myMutationOp = new MyMutationOp(0.3)
    val encodingSize = 16
    val bitSet = createBitSet(encodingSize, flag = true)

    println(ga.repr(bitSet, encodingSize))
    val mutatedBitSet = myMutationOp(bitSet)
    assert(bitSet.length() == mutatedBitSet.length())
    println(ga.repr(mutatedBitSet, encodingSize))
  }
/*
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
