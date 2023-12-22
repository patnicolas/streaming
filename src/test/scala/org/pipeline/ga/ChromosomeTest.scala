package org.pipeline.ga

import org.pipeline.ga
import org.pipeline.ga.Gene.BitsIntEncoder
import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class ChromosomeTest extends AnyFlatSpec{

  ignore should "Succeed managing BitSets" in {
    import java.util

    val encodingLength = 12
    val bitSet = new util.BitSet(encodingLength)

    println(repr(bitSet, encodingLength))
    bitSet.set(0)
    bitSet.set(5)
    println(repr(bitSet, encodingLength))
    bitSet.set(0)
    println(repr(bitSet, encodingLength))
  }

  ignore should "Succeed encoding an integer into a bit set" in {
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

  /*
  ignore should "Succeed encoding a type into a chromosome" in {
    import ChromosomeTest._

    val prices = List[Double](2.9, 4.0, 22.4, 1.0, 0.5, 13.7, 15.2)
    val encodingLength = 4
    val chromosomeDouble = Chromosome[Double](prices)
    println(ga.repr(chromosomeDouble., prices.length*encodingLength))
    print(s"Chromosome double: ${chromosomeDouble.repr.mkString("")}")
  }

  it should "Succeed decoding a set of bits into a Chromosome" in {
    import ChromosomeTest._

    val bitStr = "0010100101000000011001000001000000000101110101111111001"
    val bits = bitStr.toCharArray.map(_.toInt).toList
    val encodingLength = 4
    val chromosomeInt = Chromosome[Int](encodingLength)
    val newChromosome = chromosomeInt.decode(bits)
    print(s"\n${newChromosome.toString}")
  }

   */
}


private[ga] object ChromosomeTest {
  class DoubleQuantizer extends Quantizer[Double] {
    override def apply(x: Double): Int = x.floor.toInt

    override def unapply(n: Int): Double = n.toDouble
  }

  implicit val doubleQuantizer: DoubleQuantizer = new DoubleQuantizer
}
