package org.pipeline


import scala.util.Random

package object kalman{
  type DMatrix = Array[Array[Double]]
  type DVector = Array[Double]


  def identityMatrix(numRows: Int): DMatrix = {
    Array.tabulate(numRows)(
      n => {
        val row = Array.fill(numRows)(0.0)
        row(n) = 1.0
        row
      }
    )
  }

  def randMatrix(numRows: Int, mean: Double, distribution: Double => Double): DMatrix =
    Array.fill(numRows)(Array.fill(numRows)(distribution(mean)))


  def randUniformMatrix(numRows: Int): DMatrix =
    Array.fill(numRows)(Array.fill(numRows)(Random.nextDouble))

  def normalRandomValue(stdDev: Double): Double = {
    import org.apache.commons.math3.distribution.NormalDistribution

    new NormalDistribution(0.0, stdDev).density(Random.nextDouble)
  }
}
