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
package org.streaming.spark

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.streaming.OutputMode
import org.streaming.spark.SparkStructStreams.{SAggregator, STransform}


/**
 * Generic wrapper for structured streaming
 * outputMode  Mode for writer stream (i.e. Append, Update, ...)
 * outputFormat  Format used by the stream writer (json, console, csv, ...)
 * outputColumn Name of the aggregated column
 * isConsoleSink Flag to enabled Console sink
 * transform  Optional transformation (input dataframe, SQL statement) => Output data frame
 * aggregator Optional aggregator with groupBy (single column) and sql.functions._
 *                   aggregation function
 * @author Patrick Nicolas
 */
trait SparkStructStreams{
  val outputMode: OutputMode
  val outputFormat: String
  val outputColumn: String
  val isConsoleSink: Boolean
  val transform: Option[STransform]
  val aggregator: Option[SAggregator]
}


object  SparkStructStreams {

  /**
   * Transform (DataFrame, temporary table name) => DataFrame
   */
  private type TransformFunc = (DataFrame, String) => DataFrame

  /**
   * Transformer for DataFrame (map function) using SQL.
   * {{{
   *    The selects fields and whereConditions are concatenate for the SQL statement
   *    There is no validation of the generated SQL query prior execution
   * }}}
   *
   * @param selectFields    List of fields to display
   * @param whereConditions WHERE conditions  if not empty
   * @param transformFunc   DataFrame transformation function DataFrame => DataFrame
   * @param descriptor      Optional descriptor
   * @author Patrick Nicolas
   */
  final class STransform(
    selectFields: Seq[String],
    whereConditions: Seq[String],
    transformFunc: TransformFunc,
    descriptor: String = ""
  ){
    def apply(df: DataFrame): DataFrame = transformFunc(df, queryStmt)

    override def toString: String = s"$descriptor: $queryStmt"

    private def queryStmt: String = {
      val whereConditionStr = if (whereConditions.nonEmpty) s"WHERE ${whereConditions.mkString("AND ")}" else ""
      s"SELECT ${selectFields.mkString(",")} FROM temptable $whereConditionStr"
    }
  }

  /**
   * Action or aggregator (Column, Column)
   */
  private type AggrFunc = Column => Column

  /**
   * Aggregator class
   *
   * @param groupByCol    Column used for grouping
   * @param aggrCol       Column for aggregation
   * @param aggrFunc      Aggregation function of type ColumnType => ColumnType
   * @param aggrAliasName Alias name for the aggregated values
   * @author Patrick Nicolas
   */
  class SAggregator(
    groupByCol: Column,
    aggrCol: Column,
    aggrFunc: AggrFunc,
    aggrAliasName: String
  ){
    def apply(inputDF: DataFrame): DataFrame = inputDF.groupBy(groupByCol).agg(aggrFunc(aggrCol).alias(aggrAliasName))

    override def toString: String = s"$aggrAliasName: ${groupByCol.toString} => ${aggrCol.toString}"
  }
}
