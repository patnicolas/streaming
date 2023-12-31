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
package org.pipeline.ga

trait ScoreOp{
self =>
  def apply[T : Ordering, U : Ordering](population: Chromosome[T, U]): Chromosome[T, U] = ???

  {

  }
    // 1. Convert to Spark variable structure
    // 2. Save into  configuration file
    // 3. Execute Spark submit
}


private[ga] object ScoreOp {

}
