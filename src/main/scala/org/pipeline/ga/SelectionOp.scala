package org.pipeline.ga

trait SelectionOp{
self =>

  def apply[T, U](chromosomes: Seq[Chromosome[T, U]]): Seq[Chromosome[T, U]] = {
    chromosomes.
  }
}
