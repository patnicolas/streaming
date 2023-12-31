package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class SelectionOpTest extends AnyFlatSpec{

  it should "Succeed initializing a set of chromosome" in {

    println(s"${null.asInstanceOf[Double]}, ${null.asInstanceOf[String]}")

    val initialPopulationSize = 4
    val selector = new SelectionOp {
      override val maxPopulationSize: Int = initialPopulationSize
    }

    val idsT = Seq.tabulate(5)(n => s"i$n")
    val quantizer1 = new QuantizerInt(encodingLength = 4, maxValue = 6)
    val idsU = Seq.tabulate(3)(n => s"f$n")
    val quantizer2 = new QuantizerDouble(encodingLength = 4, scaleFactor = 1.0, maxValue = 6)
    val chromosome = selector[Int, Double](idsT, quantizer1, idsU, quantizer2)
    println(chromosome.toString)
  }

  it should "Succeed ranking a set of chromosome" in {
    import scala.util.Random

    val idsT = Seq.tabulate(5)(n => s"i$n")
    val quantizer1 = new QuantizerInt(encodingLength = 4, maxValue = 6)
    val idsU = Seq.tabulate(3)(n => s"f$n")
    val quantizer2 = new QuantizerDouble(encodingLength = 4, scaleFactor = 1.0, maxValue = 6)
    val chromosomes = Seq.tabulate(
      18
    )(n => Chromosome[Int, Double](idsT, quantizer1, idsU, quantizer2))
    assert(chromosomes.length == 18)
    chromosomes.foreach(_.fitness = Random.nextDouble*36)

    val initialPopulationSize = 10
    val selector = new SelectionOp{
      override val maxPopulationSize: Int = initialPopulationSize
    }
    val selectedChromosomes = selector[Int, Double](chromosomes)
    println(selectedChromosomes.map(_.toString).mkString("\n"))
  }
}
