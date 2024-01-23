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

import org.apache.spark.ml.classification.LogisticRegression

class LogisticRegressionGenerator {
  val sgdLogisticRegression = {
    val logisticRegression = new LogisticRegression()
      .setFamily("binomial")
      .setMaxIter(1000)
      .setTol(0.00001)
    println(logisticRegression.params.mkString("\n"))
    logisticRegression
  }
}