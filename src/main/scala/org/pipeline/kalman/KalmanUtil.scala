package org.pipeline.kalman

import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrices}

private[kalman] object KalmanUtil{

  /**
   *
   * @param a
   * @param b
   * @return */
  @throws(clazz = classOf[IllegalArgumentException])
  def add(a: DenseMatrix, b: DenseMatrix): DenseMatrix = {
    require(
      a.numCols == b.numCols && a.numRows == b.numRows,
      s"Number of rows/cols in matrices ${a.toString} and ${b.toString} are different"
    )
    val c: Array[Double] = a.values.indices.map(index => a.values(index) + b.values(index)).toArray
    new DenseMatrix(a.numRows, a.numCols, c)
  }

  /**
   *
   * @param a
   * @param b
   * @return */
  @throws(clazz = classOf[IllegalArgumentException])
  def add(a: DenseVector, b: DenseVector): DenseVector = {
    require(a.size == b.size, s"Size of vector ${a.toString} and ${b.toString} are different")

    val c: Array[Double] = a.values.indices.map(index => a.values(index) + b.values(index)).toArray
    new DenseVector(c)
  }

  def inv(a: DenseMatrix): DenseMatrix = {
    val invValues = a.values.map(x => if (math.abs(x) < 1e-18) 0.0 else 1.0 / x)
    new DenseMatrix(a.numRows, a.numCols, invValues)
  }

  @throws(clazz = classOf[IllegalArgumentException])
  def add(a: DenseMatrix, b: DenseVector): DenseMatrix = {
    require(a.numRows == b.size, s"Size of vector ${a.toString} and ${b.toString} are different")

    val bMatrix: DenseMatrix = Matrices.eye(b.size).asInstanceOf[DenseMatrix]
    add(a, bMatrix)
  }


  @throws(clazz = classOf[IllegalArgumentException])
  def subtract(a: DenseMatrix, b: DenseMatrix): DenseMatrix = {
    require(
      a.numCols == b.numCols && a.numRows == b.numRows,
      s"Number of rows/cols in matrices ${a.toString} and ${b.toString} are different"
    )

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
