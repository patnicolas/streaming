package org.pipeline.ga

trait ScoreOp{
self =>
  def apply[T : Ordering, U : Ordering](population: Chromosome[T, U]): Chromosome[T, U] = ???
    // 1. Convert to Spark variable structure
    // 2. Save into  configuration file
    // 3. Execute Spark submit



}
