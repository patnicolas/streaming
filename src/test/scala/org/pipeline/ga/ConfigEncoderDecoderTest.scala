package org.pipeline.ga

import org.pipeline.streams.spark
import org.pipeline.streams.spark.SparkConfiguration
import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class ConfigEncoderDecoderTest extends AnyFlatSpec {
  import ConfigEncoderDecoderTest._

  it should "Succeed encoding a Spark configuration parameters set 1" in {
    val parameterDefinitions = Seq[spark.ParameterDefinition](
      parameterDefinition1,
      parameterDefinition2,
      parameterDefinition3
    )
    val mySparkConfiguration = SparkConfiguration(parameterDefinitions)
    println(s"Initial configuration:\n${mySparkConfiguration.toString}")

    val chromosome = ConfigEncoderDecoder.encode(mySparkConfiguration)
    println(chromosome.toString)
    val decodedChromosome = ConfigEncoderDecoder.decode(chromosome, mySparkConfiguration)
    println(s"Decoded configuration:\n${decodedChromosome.toString}")
  }
}


private[ga] object ConfigEncoderDecoderTest {

  private val parameterDefinition1: spark.ParameterDefinition = spark.ParameterDefinition(
    "param1",
    "40",
    isDynamic = true,
    "Int",
    Seq[String]("20", "30", "40")
  )

  private val parameterDefinition2: spark.ParameterDefinition = spark.ParameterDefinition(
    "param2",
    "2",
    isDynamic = true,
    "Int",
    Seq[String]("1", "2", "3", "4")
  )

  private val parameterDefinition3: spark.ParameterDefinition = spark.ParameterDefinition(
    "param3",
    "0.1",
    isDynamic = true,
    "Float",
    Seq[String]("0.05", "0.1", "0.15", "0.2", "0.25")
  )
}


