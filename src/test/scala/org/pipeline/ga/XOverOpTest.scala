package org.pipeline.ga

import org.pipeline.ga
import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class XOverOpTest extends AnyFlatSpec{
  import XOverOpTest._

  it should "Succeed cross-over two chromosomes" in {
    val xOverOp = new MyXOverOp(0.9)
    val encodingSize = 16
    val bitSet1 = createBitSet(encodingSize, flag = true)
    println(ga.repr(bitSet1, encodingSize))
    val bitSet2 = createBitSet(encodingSize, flag = false)
    println(ga.repr(bitSet2, encodingSize))
    val (offSpring1, offSpring2) = xOverOp(bitSet1, bitSet2, encodingSize)
    println(s"First Offspring: ${ga.repr(offSpring1, encodingSize)}")
    println(s"Second Offspring: ${ga.repr(offSpring2, encodingSize)}")
  }
}

private[ga] object XOverOpTest {
  import java.util
  class MyXOverOp(override val xOverProbThreshold: Double) extends XOverOp

  def createBitSet(numBits: Int, flag: Boolean): util.BitSet = {
    val bitSet = new util.BitSet(numBits)

    (0 until numBits).foldLeft(bitSet)((bs, index) => {
      bs.set(index, flag)
      bs
    })
  }
}
