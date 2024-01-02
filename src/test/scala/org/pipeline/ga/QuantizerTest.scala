package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class QuantizerTest extends AnyFlatSpec{

  it should "Succeed quantizing a floating point value with constraint without scale factor" in {
    val input = 4.5F
    val validRange = Seq[Float](6.0F, 8.0F, 10.0F, 12.0F)
    val quantizer = new QuantizerFloat(5, scaleFactor = 1.0F, validRange)
    val bitsRepr = quantizer(input)
    println(bitsRepr.toString)
    val value = quantizer.unapply(bitsRepr)
    assert(value == input)
  }


  it should "Succeed quantizing a floating point value with constraint with scale factor" in {
    val input = 1.5F
    val validRange = Seq[Float](6.0F, 8.0F, 10.0F, 12.0F)
    val quantizer = new QuantizerFloat(5, scaleFactor = 10.0F, validRange)
    val bitsRepr = quantizer(input)
    println(bitsRepr.toString)
    val value = quantizer.unapply(bitsRepr)
    assert(value == input)
  }

  it should "Succeed quantizing a floating point value out constraint without scale factor" in {
    var condition = false
    try {
      val validRange = Seq[Float](10.0F, 15.0F, 20.0F, 30.0F)
      val quantizer = new QuantizerFloat(5, scaleFactor = 1.0F, validRange)
    } catch {
      case e: GAException => condition = true
    }
    finally {
      assert(condition)
    }
  }
}
