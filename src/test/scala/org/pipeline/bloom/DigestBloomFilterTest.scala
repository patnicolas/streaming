package org.pipeline.bloom

import org.scalatest.flatspec.AnyFlatSpec

import java.security.MessageDigest
import scala.util.Random

private[bloom] final class DigestBloomFilterTest extends AnyFlatSpec{

  ignore should "Succeed converting array of bytes from/to int" in {
    import DigestBloomFilter._
    val n = 34
    val bytes = int2Bytes(n)
    val result = bytes2Int(bytes)
    assert(n == result)
  }

  ignore should "Succeed adding items into the bloom filter" in {
    val filter = new DigestBloomFilter[Long](100, 100)

    val newValues = Array[Long](5L, 97L, 91L, 23L, 67L, 33L)
    filter.addAll(newValues)
    assert(filter.mightContain(5))
    assert(filter.mightContain(97))
    assert(!filter.mightContain(22))
  }


  ignore should "Succeed populating the bloom filter" in {
    import org.pipeline.streams.spark.implicits._
    import sparkSession.implicits._

    val bloomFilter = new DigestBloomFilter[Int](1000, 500, SHA1Algo())
    val data: Seq[Int] = Seq.tabulate(100)(n => n*n)
    val shuffleData = Random.shuffle(data)
    bloomFilter.addAll(shuffleData.toArray)
    println(bloomFilter.toString)
  }
}
