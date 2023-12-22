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

import java.util
import scala.util.Random

private[ga] trait XOverOp extends GAOp {
self =>
  val xOverProbThreshold: Double

  def apply(bitSet1: util.BitSet, bitSet2: util.BitSet, encodingSize: Int): (util.BitSet, util
  .BitSet) =
    if(rand.nextDouble < xOverProbThreshold) {
      val xOverIndex = (encodingSize*Random.nextDouble).toInt
      val bitSet1Top = bitSet1.get(0, xOverIndex)
      val bitSet2Top = bitSet2.get(0, xOverIndex)
      val bitSet1Bottom = bitSet1.get(xOverIndex, encodingSize)
      val bitSet2Bottom = bitSet2.get(xOverIndex, encodingSize)
      val offSpring1 = XOverOp.xOver(bitSet1Top, xOverIndex, bitSet2Bottom, encodingSize)
      val offSpring2 = XOverOp.xOver(bitSet2Top, xOverIndex, bitSet1Bottom, encodingSize)

      (offSpring1, offSpring2)
    }
    else
      (bitSet1, bitSet2)
}


private[ga] object XOverOp {
  private def xOver(
    bitSet1: util.BitSet,
    encoding1Size: Int,
    bitSet2: util.BitSet,
    encodingSize: Int): util.BitSet = {

    (encoding1Size until encodingSize).foldLeft(bitSet1)(
      (offSpring, index) => {
        offSpring.set(index, bitSet2.get(index))
        offSpring
      }
    )
  }
}
