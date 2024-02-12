package org.pipeline

import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrices}

import scala.util.Random

package object kalman{
  type DMatrix = Array[Array[Double]]


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

  /**
   *
   * @param a
   * @param b
   * @return
   */
  @throws(clazz = classOf[IllegalArgumentException])
  def add(a: DenseMatrix, b: DenseMatrix): DenseMatrix = {
    require(a.numCols == b.numCols && a.numRows == b.numRows,
      s"Number of rows/cols in matrices ${a.toString} and ${b.toString} are different")
    val c: Array[Double] = a.values.indices.map(index => a.values(index) + b.values(index)).toArray

    new DenseMatrix(a.numRows, a.numCols, c)
  }

  /**
   *
   * @param a
   * @param b
   * @return
   */
  @throws(clazz = classOf[IllegalArgumentException])
  def add(a: DenseVector, b: DenseVector): DenseVector = {
    require(a.size == b.size, s"Size of vector ${a.toString} and ${b.toString} are different")

    val c: Array[Double] = a.values.indices.map(index => a.values(index) + b.values(index)).toArray
    new DenseVector(c)
  }

  @throws(clazz = classOf[IllegalArgumentException])
  def add(a: DenseMatrix, b: DenseVector): DenseMatrix = {
    require(a.numRows == b.size, s"Size of vector ${a.toString} and ${b.toString} are different")

    val bMatrix: DenseMatrix = Matrices.eye(b.size).asInstanceOf[DenseMatrix]
    add(a, bMatrix)
  }


  @throws(clazz = classOf[IllegalArgumentException])
  def subtract(a: DenseMatrix, b: DenseMatrix): DenseMatrix = {
    require(a.numCols == b.numCols && a.numRows == b.numRows,
      s"Number of rows/cols in matrices ${a.toString} and ${b.toString} are different")

    val c: Array[Double] = a.values.indices.map(index => a.values(index) - b.values(index)).toArray
    new DenseMatrix(a.numRows, a.numCols, c)
  }


  @throws(clazz = classOf[IllegalArgumentException])
  def subtract(a: DenseVector, b: DenseVector): DenseVector = {
    require(a.size == b.size, s"Size of vector ${a.toString} and ${b.toString} are different")

    val c: Array[Double] = a.values.indices.map(index => a.values(index) + b.values(index)).toArray
    new DenseVector(c)
  }
}
