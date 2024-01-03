package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class GAEncoderTest extends AnyFlatSpec{

  it should "Succeed encoding a floating point value with constraint without scale factor" in {
    val input = 4.5F
    val validRange = Seq[Float](6.0F, 8.0F, 10.0F, 12.0F)
    val gaEncoder = new GAEncoderFloat(5, scaleFactor = 1.0F, validRange)
    val bitsRepr = gaEncoder(input)
    println(bitsRepr.toString)
    val value = gaEncoder.unapply(bitsRepr)
    assert(value == input)
  }


  it should "Succeed encoding a floating point value with constraint with scale factor" in {
    val input = 1.5F
    val validRange = Seq[Float](6.0F, 8.0F, 10.0F, 12.0F)
    val gaEncoder = new GAEncoderFloat(5, scaleFactor = 10.0F, validRange)
    val bitsRepr = gaEncoder(input)
    println(bitsRepr.toString)
    val value = gaEncoder.unapply(bitsRepr)
    assert(value == input)
  }

  it should "Succeed encoding a floating point value out constraint without scale factor" in {
    var condition = false
    try {
      val validRange = Seq[Float](10.0F, 15.0F, 20.0F, 30.0F)
      new GAEncoderFloat(5, scaleFactor = 1.0F, validRange)
    } catch {
      case e: GAException => condition = true
    }
    finally {
      assert(condition)
    }
  }
}
