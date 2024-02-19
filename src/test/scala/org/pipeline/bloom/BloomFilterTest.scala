package org.pipeline.bloom

import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Random

private[bloom] final class BloomFilterTest extends AnyFlatSpec{

  it should "Succeed converting array of bytes from/to int" in {
    import BloomFilter._
    val n = 34
    val bytes = int2Bytes(n)
    val result = bytes2Int(bytes)
    assert(n == result)
  }

  it should "Succeed adding items into the bloom filter" in {
    import org.pipeline.streams.spark.implicits._
    val filter = new BloomFilter[Long](100, 100)

    val newValues = Array[Long](5L, 97L, 91L, 23L, 67L, 33L)

    filter.+(newValues)
    assert(filter.contains(5))
    assert(filter.contains(97))
    assert(!filter.contains(22))
  }

  ignore should "Succeed populating the bloom filter" in {
    import org.pipeline.streams.spark.implicits._

    val bloomFilter = new BloomFilter[Int](1000, 500, SHA1Algo())
    val data: Seq[Int] = Seq.tabulate(100)(n => n*n)
    val shuffleData = Random.shuffle(data)
    bloomFilter.+(shuffleData.toArray)
    println(bloomFilter.toString)
  }
}