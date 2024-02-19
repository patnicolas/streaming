package org.pipeline.bloom

import org.apache.spark.sql.SparkSession

import java.math.BigInteger
import java.security.MessageDigest
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.reflect.ClassTag.Any
import scala.util.Try


trait HashingAlgo

case class SHA1Algo() extends HashingAlgo {
  override def toString: String = "SHA1"
}
case class MD5Algo() extends HashingAlgo {
  override def toString: String = "MD5"
}


/**
 * Implementation of the Bloom filter
 * @param length Length or capacity of the Bloom filter
 * @param numHashFunctions Number of hash functions
 * @param hashingAlgo Hashing algorithm SHA1, MD5, ..
 * @param sparkSession Implicit reference to the current Spark Session
 * @author Patrick Nicolas
 */
private[bloom] final class BloomFilter[T: ClassTag](
  length: Int,             // Length or capacity of the Bloom filter
  numHashFunctions: Int,   // Number of hash functions
  hashingAlgo: HashingAlgo = SHA1Algo()  // Hashing algorithm SHA1, MD5, ..
)(implicit sparkSession: SparkSession) {
  import BloomFilter._
  require(hashingAlgo.isInstanceOf[SHA1Algo], s"Only SHA1 digest is currently supported")

  private[this] val set = new Array[Byte](length)
  private[this] val digest = Try(MessageDigest.getInstance(hashingAlgo.toString))
  private[this] var size: Int = 0

  /**
   * Add a new element of type T to the set of the Bloom filter
   * @param t New element to be added
   */
  def +(t: T): Unit = {
    hashToArray(t).foreach(set(_) = 1)
    size += 1
  }

  /**
   * Add an array of elements of type T to the
   * @param elements Elements to be added in the filter
   * @return Updated number of elements in the filter if digest is defined, -1 otherwise
   */
  def +(elements: Array[T]): Int =
    if(elements.length > 0)
      digest.map(
      _ => {
        elements.foreach(+)
        size
      }
    ).getOrElse(-1)
    else
      size

  @inline
  def getSize: Int = size

  /**
   * Test whether the element t is contained in the
   * @param t Element evaluated
   * @return true if the digest is properly instantiated and the element t has been added to
   *         the filter, false otherwise
   */
  def contains(t: T): Boolean =
    digest.map(_ => hashToArray(t).forall(set(_) == 1)).getOrElse(false)


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