package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class SelectionOpTest extends AnyFlatSpec{

  it should "Succeed initializing a set of chromosome" in {

    println(s"${null.asInstanceOf[Double]}, ${null.asInstanceOf[String]}")

    val initialPopulationSize = 4
    val selector = new SelectionOp {
      override val maxPopulationSize: Int = initialPopulationSize
    }

    val validRange1 = Seq[Int](6, 8, 10, 12)
    val idsInt = Seq.tabulate(5)(n => s"i$n")
    val gaEncoderInt = new GAEncoderInt(encodingLength = 4, validRange1)
    val idsFloat = Seq.tabulate(3)(n => s"f$n")

    val validRange2 = Seq[Float](10.0F, 15.0F, 20.0F, 30.0F)
    val gaEncoderFloat = new GAEncoderFloat(encodingLength = 4, scaleFactor = 1.0F, validRange2)
    val chromosome = selector.rand(
      idsInt,
      Seq[GAEncoderInt](gaEncoderInt),
      idsFloat,
      Seq[GAEncoderFloat](gaEncoderFloat))
    println(chromosome.toString)
  }

  it should "Succeed ranking a set of chromosome" in {
    import scala.util.Random

    val idsInt = Seq.tabulate(5)(n => s"i$n")
    val validRange1 = Seq[Int](6, 8, 10, 12)
    val gaEncoderInt = new GAEncoderInt(encodingLength = 4, validRange1)
    val idsFloat = Seq.tabulate(3)(n => s"f$n")

    val validRange2 = Seq[Float](2.0F, 4.0F, 6.0F, 8.0F)
    val gaEncoderFloat = new GAEncoderFloat(encodingLength = 4, scaleFactor = 1.0F, validRange2)
    val chromosomes = Seq.tabulate(
      18
    )(n => Chromosome[Int, Float](
      idsInt,
      Seq[GAEncoderInt](gaEncoderInt),
      idsFloat,
      Seq[GAEncoderFloat](gaEncoderFloat)))
    assert(chromosomes.length == 18)
    chromosomes.foreach(_.fitness = Random.nextDouble*36)

    val initialPopulationSize = 10
    val selector = new SelectionOp{
      override val maxPopulationSize: Int = initialPopulationSize
    }
    val selectedChromosomes = selector.select(chromosomes)
    println(selectedChromosomes.map(_.toString).mkString("\n"))
  }
}
