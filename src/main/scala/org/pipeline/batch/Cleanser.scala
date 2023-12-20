package org.pipeline.batch

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Cleanser extends App{

  implicit val sparkSession: SparkSession = {
    val conf = new SparkConf().setMaster("local[8]")
    SparkSession.builder()
      .appName("sparkjob")
      .config(conf)
      .getOrCreate()
  }

  println(args.mkString(", "))
  val srcFilename = args(0)
  private val outputFilename = args(1)

  private val outlierRemoval = OutlierRemoval()
  outlierRemoval(srcFilename, outputFilename)
  Thread.sleep(4000L)
  sparkSession.close()
}
