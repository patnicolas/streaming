package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class QuantizerTest extends AnyFlatSpec{

  it should "Succeed quantizing a floating point value with constraint without scale factor" in {
    val input = 12.0
    val quantizer = new QuantizerDouble(5, scaleFactor = 1.0, 20.0)
    val bitsRepr = quantizer(input)
    println(bitsRepr.toString)
    val value = quantizer.unapply(bitsRepr)
    assert(value == input)
  }


  it should "Succeed quantizing a floating point value with constraint with scale factor" in {
    val input = 1.5
    val quantizer = new QuantizerDouble(5, scaleFactor = 10.0, 20.0)
    val bitsRepr = quantizer(input)
    println(bitsRepr.toString)
    val value = quantizer.unapply(bitsRepr)
    assert(value == input)
  }

  it should "Succeed quantizing a floating point value out constraint without scale factor" in {
    var condition = false
    try {
      val input = 64.0
      val quantizer = new QuantizerDouble(5, scaleFactor = 1.0, 20.0)
    } catch {
      case e: GAException => condition = true
    }
    finally {
      assert(condition)
    }
  }
}
