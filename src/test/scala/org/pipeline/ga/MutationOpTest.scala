package org.pipeline.ga

import org.pipeline.ga.MutationOpTest.{MyMutationOp, createBitSet}
import org.scalatest.flatspec.AnyFlatSpec

import java.util

private[ga] final class MutationOpTest extends AnyFlatSpec{

  it should "Succeed mutating a chromosome" in {
    val myMutationOp = new MyMutationOp(0.3)
    val bitSet = createBitSet(16, flag = true)
    val originalLen = bitSet.length()
    println(Chromosome.repr(bitSet))
    val mutatedBitSet = myMutationOp(bitSet)
    assert(bitSet.length() == mutatedBitSet.length())
    println(Chromosome.repr(mutatedBitSet))
  }
}


private[ga] object MutationOpTest  {
  class MyMutationOp(override val mutationProbThreshold: Double) extends MutationOp

  def createBitSet(numBits: Int, flag: Boolean): util.BitSet = {
    (0 until numBits).foldLeft(new util.BitSet)(
      (bs, index) => {
        bs.set(index, flag)
        bs
      }
    )
  }
}
