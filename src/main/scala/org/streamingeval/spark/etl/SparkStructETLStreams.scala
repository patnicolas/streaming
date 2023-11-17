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
package org.streamingeval.spark.etl

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.slf4j.LoggerFactory
import org.streamingeval.spark.SparkStructStreams
import org.streamingeval.spark.SparkStructStreams.{SAggregator, STransform}
import org.streamingeval.spark.etl.SparkStructETLStreams.logger


/**
 * Spark streaming from JSON representation of a dataframe inm CSV format
 * {{{
 *   The schema for the input dataset is inferred in the constructor by  reading the first JSON
 *   record
 *   The streaming pipeline is defined as
 *        1- Stream reader ( <- JSON records)
 *        2- DataFrame transformation
 *        3- DataFrame aggregation
 *        4- Stream console writer for debugging
 *        5- Stream writer of results into CSV file
 *   The parameters for the streaming pipeline are defined as arguments of the constructor
 * }}}
 *
 * @param folderPath Absolute path for the source file
 * @param outputMode  Mode for writer stream (i.e. Append, Update, ...)
 * @param outputFormat  Format used by the stream writer (json, console, csv, ...)
 * @param outputColumn Name of the aggregated column
 * @param isConsoleSink Flag to enabled Console sink
 * @param transform  Optional transformation (input dataframe, SQL statement) => Output data frame
 * @param aggregator Optional aggregator with groupBy (single column) and sql.functions._
 *                   aggregation function
 * @param sparkSession Implicit reference to the current Spark context
 *
 * @author Patrick Nicolas
 * @version 0.1
 */
final class SparkStructETLStreams private (
  folderPath: String,  // Absolute path for the source file
  override val outputMode: OutputMode, // Mode for writer stream (i.e. Append, Update, ...)
  override val outputFormat: String, //  Format used by the stream writer (json, console, csv, ...)
  override val outputColumn: String, // ame of the aggregated column
  override val isConsoleSink: Boolean,
  override val transform: Option[STransform], // Transformation (DataFrame, SQL) =>  DataFrame
  override val aggregator: Option[SAggregator] // groupBy (single column) +  sql.functions._
  // aggregator
)(implicit  sparkSession: SparkSession)  extends SparkStructStreams {

  // Extract schema from files
  private[this] lazy val schema: StructType = {
    val df = sparkSession.read.json(s"file://${folderPath}").head()
    df.schema
  }
  sparkSession.sparkContext.setLogLevel("ERROR")

  def getSchema: StructType = schema

  /**
   * Simple query of all the field from a folder
   */
  def default_read(): Unit = {
    val readDF = sparkSession.readStream.schema(schema).json(s"file://$folderPath")
    assert(readDF.isStreaming)

    val query = readDF.writeStream
          .outputMode(outputMode)
          .format(outputFormat)
          .option("checkpointLocation", "~/temp")
          .start()

    query.awaitTermination()
  }


  /**
   *  Query the content of the file using a SQL statement. This implementation relies on a
   *  temporary table.
   *  {{{
   *    There are 5 steps
   *    1- Read stream from a set of JSON file defined in  'folderPath''. The schema was inferred
   *        from the JSON files in the constructor
   *    2- Apply the optional transformation on DataFrame produced in 1)
   *    3- Apply the aggregator as a combination of a groupBy and aggregator
   *    4- Use Console stream writer for debugging purpose
   *    5- Save resulting DataFrame in CSV file using a stream writer in Update mode
   *  }}}
   */
  def execute(): Unit = {
    logger.info("Starts reading file")

    // Step 1: Stream reader from a file 'folderPath'
    val readDF:  DataFrame = sparkSession
      .readStream
      .schema(schema)
      .json(s"file://$folderPath")
    assert(readDF.isStreaming)

    // Step 2: Transform
    val transformedDF: DataFrame = transform
      .map(_(readDF))
      .getOrElse(readDF)

    // Step 3: Aggregator
    val aggregatorDF = aggregator.map(_(transformedDF)).getOrElse(transformedDF)

    // Step 4: Debugging to console
    debugSink(aggregatorDF)

    // Step 5: Stream writer into a table
    val query = aggregatorDF
      .writeStream
      .outputMode(OutputMode.Update())
      .foreachBatch{
        (batchDF: DataFrame, batchId: Long) =>
          batchDF.persist()
          batchDF.select(outputColumn)
            .write
            .mode(SaveMode.Overwrite)
            .format(outputFormat)
            .save(path = s"temp/$outputFormat")
          batchDF.unpersist()
          ()
      }
      .trigger(Trigger.ProcessingTime("4 seconds"))
      .start()
    logger.info("Sink started")
    query.awaitTermination()
  }

  private def debugSink(df: DataFrame): Unit =
    if (isConsoleSink)
      df.writeStream.outputMode(OutputMode.Complete()).format("console").start()
}


/**
 *  Singleton for various constructor
 */
object  SparkStructETLStreams{
  final private val logger = LoggerFactory.getLogger(classOf[SparkStructETLStreams])

  def apply(
    folderPath: String,
    outputMode: OutputMode,
    outputFormat: String,
    outputTable: String,
    debug: Boolean,
    transform: STransform,
    aggregator: SAggregator,
  )
    (implicit sparkSession: SparkSession): SparkStructETLStreams = {
    new SparkStructETLStreams(
      folderPath,
      outputMode,
      outputFormat,
      outputTable,
      debug,
      Some(transform),
      Some(aggregator)
    )
  }

  def apply(
    folderPath: String,
    outputMode: OutputMode,
    outputFormat: String,
    outputFolder: String,
    transform: STransform
  )(implicit sparkSession: SparkSession): SparkStructETLStreams = {
    new SparkStructETLStreams(
      folderPath,
      outputMode,
      outputFormat,
      outputFolder,
      isConsoleSink = true,
      Some(transform),
      None
    )
  }

  def apply(
    folderPath: String,
    outputMode: OutputMode,
    outputFormat: String,
    outputFolder: String,
  )
    (implicit sparkSession: SparkSession): SparkStructETLStreams = {
    new SparkStructETLStreams(
      folderPath,
      outputMode,
      outputFormat,
      outputFolder,
      isConsoleSink = true,
      None,
      None
    )
  }
}
