/**
 * Copyright 2022,2024 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.pipeline.ml

import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.Random

class GaussGenerator(mean1: Double, mean2: Double, sigma: Double)(implicit
  sparkSession: SparkSession) {
  def apply(numDataPoints: Int): Dataset[LabeledPoint] = {
    import sparkSession.implicits._
    val halfNumDataPoints = numDataPoints>>1
    // Features data
    val firstDataset = Seq.fill(halfNumDataPoints)(Array[Double](rand1, rand1, rand1))
    val secondDataset = Seq.fill(halfNumDataPoints)(Array[Double](rand2, rand2, rand2))
    // Labels
    val firstLabels = Seq.fill(halfNumDataPoints)(0.0)
    val secondLabels = Seq.fill(halfNumDataPoints)(1.0)

    (firstLabels ++ secondLabels).zip(firstDataset ++ secondDataset)
      .map{ case (y, x) => LabeledPoint(y, new DenseVector(x)) }.
      toDS()
  }

  private def rand1: Double = mean1 + sigma*(Random.nextDouble()-0.5)
  private def rand2: Double = mean2 + sigma*(Random.nextDouble()-0.5)
}
