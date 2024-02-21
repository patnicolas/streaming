/**
 * Copyright 2022,2024 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.pipeline.bloom

import java.math.BigInteger
import java.security.MessageDigest
import scala.language.implicitConversions
import scala.reflect.ClassTag
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
 * @author Patrick Nicolas
 */
private[bloom] final class DigestBloomFilter[T: ClassTag](
  length: Int,             // Length or capacity of the Bloom filter
  numHashFunctions: Int,   // Number of hash functions
  hashingAlgo: HashingAlgo = SHA1Algo()  // Hashing algorithm SHA1, MD5, ..
) extends TBloomFilter[T] {
  import DigestBloomFilter._
  require(hashingAlgo.isInstanceOf[SHA1Algo], s"Only SHA1 digest is currently supported")

  private[this] val set: Array[Byte] = new Array[Byte](length)
  private[this] val digest = Try(MessageDigest.getInstance(hashingAlgo.toString))
  private[this] var size: Int = 0

  /**
   * Add a new element of type T to the set of the Bloom filter
   * @param t New element to be added
   */
  override def add(t: T): Unit = {
    hashToArray(t).foreach(set(_) = 1)
    size += 1
  }


  /**
   * Add an array of elements of type T to the filter
   * @param ts Elements to be added in the filter
   * @return Updated number of elements in the filter if digest is defined, -1 otherwise
   */
  override def addAll(ts: Array[T]): Unit =
    if(ts.length > 0)
      digest.foreach(_ => ts.foreach(add))


  @inline
  def getSize: Int = size

  /**
   * Test whether the filter might contain a given element
   * @param t Element evaluated
   * @return true if the digest is properly instantiated and the element t has been added to
   *         the filter, false otherwise
   */
  override def mightContain(t: T): Boolean =
    digest.map(_ => hashToArray(t).forall(set(_) == 1)).getOrElse(false)


  override def toString: String =
    set.indices.foldLeft(new StringBuilder)((sb, index) => {
      val bytes = set.slice(index * numBytes, index * (numBytes + 1))
      sb.append(bytes2Int(bytes)).append(" ")
    }).toString


  // ---------------------   Helper private methods -----------------
  private def hashToArray(t: T): Array[Int] =
    (0 until numHashFunctions).foldLeft(new Array[Int](numHashFunctions))(
      (buf, idx) => {
        val value = if(idx > 0) hash(buf(idx -1)) else hash(t.hashCode)
        buf.update(idx, value)
        buf
      }
    )

  /**
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


private[bloom] object DigestBloomFilter {
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