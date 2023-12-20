package org.pipeline.ga

import org.pipeline.ga.Chromosome.BitsIntEncoder
import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class ChromosomeTest extends AnyFlatSpec{

  it should "Succeed encoding an integer into a bit set" in {
    val bitsIntEncoder = new BitsIntEncoder(5)

    var n = 11
    var bits: Seq[Int] = bitsIntEncoder(n)
    assert(bits.mkString("") == "01011")

    n = 4
    bits = bitsIntEncoder(n)
    assert(bits.mkString("") == "00100")

    n = 9
    bits = bitsIntEncoder(n)
    assert(bits.mkString("") == "01001")
  }

  it should "Succeed decoding bits into an integer" in {
    val bitsIntEncoder = new BitsIntEncoder(5)

    var bits = List[Int](0,1,0,1,1)
    var result = bitsIntEncoder.unapply(bits)
    assert(result == 11)
  }

  it should "Succeed encoding a type into a chromosome" in {
    val prices = List[Double](2,9, 4,0, 22,4, 1,0, 0,5, 13,7, 15,2)
    val quantization = (x: Double) => x.toInt
    val chromosomeDouble = new Chromosome[Double](prices, quantization, 4)
    print(chromosomeDouble.toString)
  }
}
