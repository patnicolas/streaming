package org.pipeline.bloom

import org.apache.spark.sql.SparkSession

import java.math.BigInteger
import java.security.MessageDigest
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.reflect.ClassTag.Any
import scala.util.Try


trait HashingAlgorithm

case object SHA1Algorithm extends HashingAlgorithm {
  override def toString: String = "SHA1"
}
case object MD5Algorithm extends HashingAlgorithm {
  override def toString: String = "MD5"
}


/**
 * Implementation of the Bloom filter
 * @param length
 * @param numHashFunctions
 * @param hashingAlgorithm
 */
private[bloom] final class BloomFilter[T: ClassTag](
  length: Int,
  numHashFunctions: Int,
  hashingAlgorithm: HashingAlgorithm = SHA1Algorithm
) {

  import BloomFilter._
  private[this] val set = new Array[Byte](length)
  private[this] val digest = Try(MessageDigest.getInstance(hashingAlgorithm.toString))
  private[this] var size: Int = 0

  def +(elements: Array[T]): Int = digest.map(
    _ => {
      elements.foreach(+)
      size
    }
  ).getOrElse(-1)


  def contains(t: T): Boolean =
    digest.map(_ =>
      hashToArray(t).forall(n =>
        set(n) == 1)
    ).getOrElse(false)

  def +(t: T): Unit = {
    hashToArray(t).foreach(set(_) = 1)
    size += 1
  }


  override def toString: String = {
    set.indices.foldLeft(new StringBuilder)((sb, index) => {
      val bytes = set.slice(index * numBytes, index * (numBytes + 1))
      sb.append(bytes2Int(bytes)).append(" ")
    }).toString
  }

  // ---------------------   Helper private methods -----------------
  private def hashToArray(t: T): Array[Int] = {
    (0 until numHashFunctions).foldLeft(new Array[Int](numHashFunctions))(
      (buf, idx) => {
        val value = if(idx > 0) hash(buf(idx -1)) else hash(t.hashCode)
        buf.update(idx, value)
        buf
      }
    )
  }

  /**
   *
   * @param value Value to hash
   * @return Has value if successful, -1 otherwise
   */
  private def hash(value: Int): Int = digest.map(
    d => {
      d.reset()
      d.update(value)
      Math.abs(new BigInteger(1, d.digest).intValue) % (set.length - 1)
    }
  ).getOrElse(-1)
}


private[bloom] object BloomFilter {
  private val numBytes: Int = 4
  private val lastByte: Int = numBytes - 1

  implicit def int2Bytes(value: Int): Array[Byte] = Array.tabulate(numBytes)(
    n => {
      val offset = (lastByte - n) << lastByte
      ((value >>> offset) & 0xFF).toByte
    }
  )


  implicit def bytes2Int(bytes: Array[Byte]): Int =
    bytes.foldLeft(0)((n, byte) => (n << 8) + (byte & 0xFF))
}