package org.pipeline.ga


import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class ChromosomeTest extends AnyFlatSpec{

  it should "Succeed instantiating a chromosome as two sequences of features" in {
    var condition = true
    try {
      val validValues = Seq[Float](8.0F, 10.0F, 12.0F)
      val gaEncoder1 = new GAEncoderFloat(encodingLength = 6, scaleFactor = 1.0F, validValues)
      val inputs1 = Seq[Float](1.0F, 3.5F, 2.5F)
      val genes1 = inputs1.map(x => Gene[Float](x.toString, x, gaEncoder1))

      val gaEncoder2 = new GAEncoderInt(4, Seq[Int](6, 8, 10))
      val inputs2 = Seq[Int](3, 0, 4)
      val genes2 = inputs2.map(x => Gene[Int](x.toString, x, gaEncoder2))

      val chromosome: Chromosome[Float, Int] = Chromosome[Float, Int](genes1, genes2)
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
      val validValues = Seq[Float](8.0F, 10.0F, 12.0F)
      val gaEncoder1 = new GAEncoderFloat(encodingLength = 6, scaleFactor = 2.0F, validValues)
      val inputs1 = Seq[Float](1.0F, 3.5F, 2.5F)
      val genes1 = inputs1.map(x => Gene[Float](x.toString, x, gaEncoder1))

      val chromosome = Chromosome[Float](genes1)
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
      val validValues = Seq[Float](8.0F, 10.0F, 12.0F)
      val gaEncoder1 = new GAEncoderFloat(encodingLength = 6, scaleFactor = 2.0F, validValues)
      val inputs1 = Seq[Float](1.0F, 3.5F, 2.5F)
      val genes1 = inputs1.map(x => Gene[Float](x.toString, x, gaEncoder1))

      val gaEncoder2 = new GAEncoderInt(encodingLength = 4, Seq[Int](4, 6, 8))
      val inputs2 = Seq[Int](3, 4, 5)
      val genes2 = inputs2.map(x => Gene[Int](x.toString, x, gaEncoder2))

      val chromosome: Chromosome[Float, Int] = Chromosome[Float, Int](genes1, genes2)
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

  it should "Succeed generating a random chromosome" in {
    val idsInt = Seq.tabulate(5)(n => s"i$n")
    val gaEncoder1 = new GAEncoderInt(encodingLength = 4, Seq[Int](4, 6, 8))
    val idsFloat = Seq.tabulate(5)(n => s"f$n")

    val validValues = Seq[Float](8.0F, 10.0F, 12.0F)
    val gaEncoder2 = new GAEncoderFloat(encodingLength = 4, scaleFactor =1.0F, validValues)
    val chromosome = Chromosome[Int, Float](
      idsInt,
      Seq[GAEncoderInt](gaEncoder1),
      idsFloat,
      Seq[GAEncoderFloat](gaEncoder2))
    println(s"Randomly initialized Chromosome ${chromosome.toString}")
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
