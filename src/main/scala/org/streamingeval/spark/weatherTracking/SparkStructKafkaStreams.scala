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
package org.streamingeval.spark.weatherTracking

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 *
 * @param inputTopics List of input topics
 * @param outputTopics List of output topics
 * @param sparkSession Implicit reference to the current Spark context
 *
 * @author Patrick Nicolas
 */
private[weatherTracking] final class SparkStructKafkaStreams(
  inputTopics: Seq[String],
  outputTopics: Seq[String])(implicit sparkSession: SparkSession) {

  private[this] val sourceDF: Option[DataFrame] = try {
    val df = sparkSession
      .read
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:8089")
      .option("subscribe", inputTopics.mkString(","))
      .load()
    Some(df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING"))
  } catch {
    case e: Exception =>
      println(e.getMessage)
      None
  }




  private def sink(df: DataFrame): Option[DataFrame] = try {
    val ds = df
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING")
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:8089")
      .option("subscribe", outputTopics.mkString(",")
    ).start()
    Some(df)
  } catch {
    case e: Exception => println(e.getMessage)
      None
  }
}
