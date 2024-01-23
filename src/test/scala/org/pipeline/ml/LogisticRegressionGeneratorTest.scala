package org.pipeline.ml

import org.scalatest.flatspec.AnyFlatSpec

class LogisticRegressionGeneratorTest extends AnyFlatSpec{

  it should "Succeed instantiating Logistic Regression" in {
    val lrGenerator = new LogisticRegressionGenerator
  }
}
