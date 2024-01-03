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
    val idsT = Seq.tabulate(5)(n => s"i$n")
    val gaEncoder1 = new GAEncoderInt(encodingLength = 4, validRange1)
    val idsU = Seq.tabulate(3)(n => s"f$n")

    val validRange2 = Seq[Float](10.0F, 15.0F, 20.0F, 30.0F)
    val gaEncoder2 = new GAEncoderFloat(encodingLength = 4, scaleFactor = 1.0F, validRange2)
    val chromosome = selector[Int, Float](idsT, gaEncoder1, idsU, gaEncoder2)
    println(chromosome.toString)
  }

  it should "Succeed ranking a set of chromosome" in {
    import scala.util.Random

    val idsT = Seq.tabulate(5)(n => s"i$n")
    val validRange1 = Seq[Int](6, 8, 10, 12)
    val gaEncoder1 = new GAEncoderInt(encodingLength = 4, validRange1)
    val idsU = Seq.tabulate(3)(n => s"f$n")

    val validRange2 = Seq[Float](2.0F, 4.0F, 6.0F, 8.0F)
    val gaEncoder2 = new GAEncoderFloat(encodingLength = 4, scaleFactor = 1.0F, validRange2)
    val chromosomes = Seq.tabulate(
      18
    )(n => Chromosome[Int, Float](idsT, gaEncoder1, idsU, gaEncoder2))
    assert(chromosomes.length == 18)
    chromosomes.foreach(_.fitness = Random.nextDouble*36)

    val initialPopulationSize = 10
    val selector = new SelectionOp{
      override val maxPopulationSize: Int = initialPopulationSize
    }
    val selectedChromosomes = selector[Int, Float](chromosomes)
    println(selectedChromosomes.map(_.toString).mkString("\n"))
  }
}
