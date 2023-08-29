package org.streamingeval.spark

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.streaming.OutputMode
import org.streamingeval.spark.SparkStructStreams.{SAggregator, STransform}


/**
 *
 */
trait  SparkStructStreams{
  val outputMode: OutputMode
  val  outputFormat: String
  val outputColumn: String
  val isConsoleSink: Boolean
  val transform: Option[STransform]
  val aggregator: Option[SAggregator]
}


object  SparkStructStreams {

  /**
   * Transform (DataFrame, temporary table name) => DataFrame */
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
   * @param descriptor      Optional descriptor */
  final class STransform(
    selectFields: Seq[String],
    whereConditions: Seq[String],
    transformFunc: TransformFunc,
    descriptor: String = ""
  ){

    def apply(df: DataFrame): DataFrame = transformFunc(
      df,
      queryStmt
    )

    override def toString: String = s"$descriptor: $queryStmt"

    private def queryStmt: String = {
      val whereConditionStr = if (whereConditions.nonEmpty) s"WHERE ${whereConditions.mkString("AND ")}" else ""
      s"SELECT ${selectFields.mkString(",")} FROM temptable $whereConditionStr"
    }
  }

  /**
   * Action or aggregator (Column, Column) */
  private type AggrFunc = Column => Column

  /**
   * Aggregator class
   *
   * @param groupByCol    Column used for grouping
   * @param aggrCol       Column for aggregation
   * @param aggrFunc      Aggregation function of type ColumnType => ColumnType
   * @param aggrAliasName Alias name for the aggregated values */
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
