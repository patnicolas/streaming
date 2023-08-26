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
package org.streamingeval.spark

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, ForeachWriter, Row, SparkSession}
import org.apache.spark.sql.types.StructType
import org.streamingeval.PatientRecord




/**
 * Spark streaming from a local file
 * @param folderPath Absolute path for the source file
 * @param sparkSession Implicit reference to the current Spark context
 *
 * @author Patrick Nicolas
 * @version 0.1
 */
final class SparkStructStreamsFromFile(folderPath: String)(implicit sparkSession: SparkSession){

  private[this] lazy val schema: StructType = {
    val df = sparkSession.read.json(s"file://${folderPath}").head()
    df.schema
  }
  sparkSession.sparkContext.setLogLevel("ERROR")

  def getSchema: StructType = schema


  def read(): Unit = {
    import sparkSession.implicits._
    val readDS = sparkSession.readStream
      .schema(schema)
      .json(s"file://${folderPath}")

    assert(readDS.isStreaming)


    val query = readDS.writeStream
      .outputMode(OutputMode.Append())
      .format("console")
      .option("checkpointLocation", "~/temp")
      .start()

    println(query.lastProgress.sources(0))

    query.awaitTermination()
  }

  def read(sqlStatement: String): Unit = {
    println("Started reading file")
    val readDS = sparkSession.readStream.schema(schema).json(s"file://${folderPath}")
    assert(readDS.isStreaming)

    val sqlQuery = s"SELECT age,gender FROM temptable WHERE age > 24"
    readDS.createOrReplaceTempView("temptable")
    val transformedDF = sparkSession.sql(sqlQuery)

    val writer = new ForeachWriter[Row] {
      override def open(partitionId: Long, version: Long): Boolean = true
      override def close(errorOrNull: Throwable): Unit = { println("closed")}
      override def process(row: Row): Unit = {
        println("***********")
        (0 until row.size).foreach(index => println(row(index)))
      }
    }



    val query = transformedDF.writeStream
      .foreach(writer)
      .outputMode(OutputMode.Append())
      .format("json")
      .trigger(Trigger.ProcessingTime("2 second"))
    //  .option("table", "outputtable")
      .option("checkpointLocation", "temp/checkpoint")
    //  .foreachBatch { case (df: Dataset[Row], batchId: Long) => df.show(2) }
      .start("temp/json")
    query.awaitTermination()
    println("Completed")
  }


  /*
  def read(queryStr: String): Dataset[Row] = {
    val streamedDS = read
    streamedDS.sqlContext.sql(queryStr)
  }



  def read[T](
    selectFields: Seq[String],
    whereCondition: String = "",
    groupByCondition: String = ""
  )(implicit encoder: Encoder[T]): Dataset[T] = {
    val streamedDS = read
    var sqlQuery = s"SELECT ${selectFields.mkString(",")} FROM temptable"
    if(whereCondition.nonEmpty)
      sqlQuery = s"${sqlQuery} WHERE $whereCondition"
    if(groupByCondition.nonEmpty)
      sqlQuery = s"${sqlQuery} GROUP BY $groupByCondition"
    streamedDS.createOrReplaceTempView("temptable")
    val df = sparkSession.sql(sqlQuery)
    df.as[T]
  }

   */



  /*
  def read(
    selectFields: Seq[String],
    whereCondition: String,
    groupByCondition: String = "",
    aggExpr: (String, String) = ("", "")): Dataset[Row] = {
    val streamedDS = read

    // If the WHERE condition is defined
    if(whereCondition.nonEmpty)
      if(groupByCondition.nonEmpty)
        streamedDS.select(selectFields.mkString(","))
          .where(whereCondition)
          .groupBy(groupByCondition)
          .agg(aggExpr)
      else
        streamedDS.select(selectFields.mkString(",")).where(whereCondition)

    // No where condition
    else
      if(groupByCondition.nonEmpty)
        streamedDS.select(selectFields.mkString(",")).groupBy(groupByCondition).agg(aggExpr)
      else
        streamedDS.select(selectFields.mkString(","))
  }

   */
}
