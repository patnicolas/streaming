/**
 * Copyright 2022,2023 Patrick R. Nicolas. All Rights Reserved.
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

import scala.util.Random

/**
 *
 */
trait MutationOp extends GAOp {
self =>
  protected[this] val mutationProbThreshold: Double
  def apply(bitSet: java.util.BitSet, encodingLength: Int, numGenes: Int): java.util.BitSet =
    if(rand.nextDouble < mutationProbThreshold) {
      val bitSetIndex = (encodingLength * Random.nextDouble).toInt + 1
      /*
      val mirrorBitSetIndex =
        if(bitSetIndex + encodingLength >= numGenes*encodingLength) bitSetIndex - encodingLength
        else bitSetIndex + encodingLength
        
       */

      bitSet.flip(bitSetIndex)
      // bitSet.flip(mirrorBitSetIndex)
      bitSet
    }
    else
      bitSet
}
