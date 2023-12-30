package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class SelectionOpTest extends AnyFlatSpec{

  it should "Succeed initializing a set of chromosome" in {
    val initialPopulationSize = 4
    val selector = new SelectionOp {
      override val maxPopulationSize: Int = initialPopulationSize
    }

    val numFirstGenes: Int = 5
    val quantizer1 = new QuantizerInt(encodingLength = 4, maxValue = 6)
    val numSecondGenes: Int = 3
    val quantizer2 = new QuantizerDouble(encodingLength = 4, scaleFactor = 1.0, maxValue = 6)
    val chromosome = selector[Int, Double](numFirstGenes, quantizer1, numSecondGenes, quantizer2)
    println(chromosome.toString)
  }

  it should "Succeed ranking a set of chromosome" in {
    import scala.util.Random

    val numFirstGenes: Int = 5
    val quantizer1 = new QuantizerInt(encodingLength = 4, maxValue = 6)
    val numSecondGenes: Int = 3
    val quantizer2 = new QuantizerDouble(encodingLength = 4, scaleFactor = 1.0, maxValue = 6)
    val chromosomes = Seq.fill(
      18
    )( Chromosome[Int, Double](numFirstGenes, quantizer1, numSecondGenes, quantizer2))
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
