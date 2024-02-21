package org.pipeline.bloom

import org.pipeline.bloom.SparkBloomFilterTest.computeExpectedFPRate
import org.scalatest.flatspec.AnyFlatSpec

private[bloom] final class SparkBloomFilterTest extends AnyFlatSpec{

  it should "Succeed creating a Bloom filter" in {
    val input = Array[Long](5L, 97L, 91L, 23L, 67L, 33L) ++
      Array.tabulate(10000)(n => n.toLong+100L)

    val filter = new SparkBloomFilter[Long](input.length, 0.05F)
    input.foreach(n => filter.add(n))
    println(filter.getExpectedFPRate)        // 0.05
    assert(filter.mightContain(97L))

    val filter2 = new SparkBloomFilter[Long](1000, 0.05F)
    input.foreach(n => filter2.add(n))  // 0.99
    println(filter2.getExpectedFPRate)

    val filter3 = new SparkBloomFilter[Long](5000, 0.05F)
    input.foreach(n => filter3.add(n)) // 0.99
    println(filter3.getExpectedFPRate)

    (1000 until 16000 by 500).foreach(
      capacity => println(s"$capacity,${computeExpectedFPRate(capacity, input)}")
    )
  }

  it should "Succeed processing a large data frame" in {

  }
}


object SparkBloomFilterTest {
  def computeExpectedFPRate(capacity: Int, input: Array[Long]): Double = {
    val filter = new SparkBloomFilter[Long](capacity, 0.05F)
    input.foreach(n => filter.add(n)) // 0.99
    filter.getExpectedFPRate
  }
}
