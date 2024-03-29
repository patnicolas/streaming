package org.pipeline.ga

import org.pipeline.streams.spark.SparkConfiguration
import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class ReproductionTest extends AnyFlatSpec{

  it should "Succeed instantiating a reproduction cycle" in {
    val execSparkSubmit: SparkConfiguration => (Int, Long) = {
      (sparkConfiguration: SparkConfiguration) => (4, 350L)
    }
    val reproduction = Reproduction(execSparkSubmit, 0.001F, 0.47F, 20)
    println(reproduction.toString)
  }


  it should "Succeed execute a reproduction cycle for randomly initialized chromosomes" in {
    val idsInt = Seq.tabulate(5)(n => s"i$n")
    val gaIntEncoder = new GAEncoderInt(encodingLength = 4, Seq[Int](4, 6, 8))

    val idsFloat = Seq.tabulate(5)(n => s"f$n")
    val validValues = Seq[Float](8.0F, 10.0F, 12.0F)
    val gaFloatEncoder = new GAEncoderFloat(encodingLength = 4, scaleFactor = 1.0F, validValues)

    val execSparkSubmit: SparkConfiguration => (Int, Long) = (_: SparkConfiguration) => (4, 350L)

    val reproduction = Reproduction(execSparkSubmit, 0.001F, 0.47F, 20)
    val optimizedChromosomes = reproduction.mate(
      idsInt,
      Seq.fill(idsInt.length)(gaIntEncoder),
      idsFloat,
      Seq.fill(idsFloat.length)(gaFloatEncoder)
    )
    println(s"Final population:\n${optimizedChromosomes.mkString("\n")}")
  }
}
