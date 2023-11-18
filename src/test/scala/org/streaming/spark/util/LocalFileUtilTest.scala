package org.streaming.spark.util

import org.scalatest.flatspec.AnyFlatSpec
import org.streaming.util.LocalFileUtil

final class LocalFileUtilTest extends AnyFlatSpec{

  it should "Succeed loading a file" in {
    val inputFile = "conf/kafkaConfig.json"
    LocalFileUtil.Load.local(inputFile).foreach(println)
  }
}
