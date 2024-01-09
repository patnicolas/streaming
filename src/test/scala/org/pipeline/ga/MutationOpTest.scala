package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec
import java.util

private[ga] final class MutationOpTest extends AnyFlatSpec{

  it should "Succeed mutating a chromosome as a bit set" in {
    val encodingLength = 5
    val validRange = Seq[Int](6, 8, 10, 12)
    val gene: Gene[Int] = Gene[Int]("id", 8, new GAEncoderInt(encodingLength, validRange))
    var condition = false
    try {
      val myMutationOp = new MutationOp{
        override val mutationProbThreshold: Double = 0.3
      }
      val mutatedGene = myMutationOp.mutate(gene)
      println(mutatedGene.toString)
      condition = true
    }
    catch {
      case e: GAException =>
        println(e.getMessage)
    } finally {
      assert(condition)
    }
  }

  it should "Succeed mutating a chromosome as a bit set with exception" in {
    val encodingLength = 5
    val validRange = Seq[Int](6, 8, 10, 12)
    val gene: Gene[Int] = Gene[Int]("id", 8, new GAEncoderInt(encodingLength, validRange))
    var condition = false
    try {
      val myMutationOp = new MutationOp{
        override val mutationProbThreshold: Double = 1.0
      }
      val mutatedGene = myMutationOp.mutate(gene)
      println(mutatedGene.toString)
    } catch {
      case e: GAException =>
        println(e.getMessage)
        condition = true
    } finally {
      assert(condition)
    }
  }

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
