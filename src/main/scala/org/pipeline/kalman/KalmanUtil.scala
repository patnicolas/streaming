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
