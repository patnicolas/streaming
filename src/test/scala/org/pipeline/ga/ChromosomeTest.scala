package org.pipeline.ga


import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class ChromosomeTest extends AnyFlatSpec{

  it should "Succeed instantiating a chromosome as two sequences of features" in {
    var condition = true
    try {
      val maxValue = 10.0
      val quantizer1 = new QuantizerDouble(6, maxValue, 2.0)
      val inputs1 = Seq[Double](1.0, 3.5, 2.5)
      val genes1 = inputs1.map(Gene[Double](_, quantizer1))

      val quantizer2 = new QuantizerInt(4, 8)
      val inputs2 = Seq[Int](3, 0, 4)
      val genes2 = inputs2.map(Gene[Int](_, quantizer2))

      val chromosome: Chromosome[Double, Int] = Chromosome[Double, Int](genes1, genes2)
      println(s"Original Chromosome ${chromosome.toString}")
    } catch {
      case e: GAException =>
        println(e.getMessage)
        condition = false
    }
    finally {
      assert(condition)
    }
  }

  it should "Succeed instantiating a chromosome as a single sequences of features" in {
    var condition = true
    try {
      val maxValue = 10.0
      val quantizer1 = new QuantizerDouble(6, maxValue, 2.0)
      val inputs1 = Seq[Double](1.0, 3.5, 2.5)
      val genes1 = inputs1.map(Gene[Double](_, quantizer1))

      val chromosome = Chromosome[Double](genes1)
      println(s"Original Chromosome ${chromosome.toString}")
    } catch {
      case e: GAException => println(e.getMessage)
        condition = false
    } finally {
      assert(condition)
    }
  }

  it should "Succeed mutating a heterogeneous chromosome" in {
    var condition = true
    try {
      val maxValue = 10.0
      val quantizer1 = new QuantizerDouble(6, maxValue, 2.0)
      val inputs1 = Seq[Double](1.0, 3.5, 2.5)
      val genes1 = inputs1.map(Gene[Double](_, quantizer1))

      val quantizer2 = new QuantizerInt(4, 8)
      val inputs2 = Seq[Int](3, 0, 4)
      val genes2 = inputs2.map(Gene[Int](_, quantizer2))

      val chromosome: Chromosome[Double, Int] = Chromosome[Double, Int](genes1, genes2)
      val mutationProb: Double = 0.9
      val mutatedChromosome = chromosome.mutate(mutationProb)
      println(s"Original: ${chromosome.toString}\nMutated:  ${mutatedChromosome.toString}")
    } catch {
      case e: GAException => println(e.getMessage)
        condition = false
    } finally {
      assert(condition)
    }
  }

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
}
