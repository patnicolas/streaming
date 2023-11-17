package org.streamingeval.spark.util

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.util.LocalFileUtil

final class LocalFileUtilTest extends AnyFlatSpec{

  it should "Succeed loading a file" in {
    val inputFile = "conf/kafkaConfig.json"
    LocalFileUtil.Load.local(inputFile).foreach(println)
  }
}
