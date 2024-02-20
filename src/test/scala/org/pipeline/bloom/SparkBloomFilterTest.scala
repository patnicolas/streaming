package org.pipeline.bloom

import org.scalatest.flatspec.AnyFlatSpec

private[bloom] final class SparkBloomFilterTest extends AnyFlatSpec{

  it should "Succeed creating a Bloom filter" in {
    val filter = new SparkBloomFilter[Long](100, 0.05F)

    val newValues = Array[Long](5L, 97L, 91L, 23L, 67L, 33L)
    newValues.foreach( filter.add(_))
    assert(filter.mightContain(97L))
  }
}
