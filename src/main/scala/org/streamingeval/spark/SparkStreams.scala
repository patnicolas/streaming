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

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies._
import org.apache.spark.streaming.{Duration, StreamingContext}



sealed trait StreamType
case class StreamFromText(directory: String) extends StreamType
case class StreamFromKafka(topics: Array[String], kafkaParams: Map[String, Object]) extends StreamType


/**
 * Generic Implementation of Direct Streams in Apache Spark
 * @param streamType Type of DStream (Kafka, Text,......)
 * @param streamingDurationMs Streaming duration in milliseconds
 * @param checkpointDir Optional directory for the checkpoint
 * @param sparkSession Implicit reference to the current Spark session
 *
 * @author Patrick Nicolas
 * @version 0.0.3
 */
private[streamingeval] final class SparkDStreams(
  streamType: StreamType,
  streamingDurationMs: Long,
  checkpointDir: Option[String])(implicit sparkSession: SparkSession) {

  private[this] val ssc = {
    val duration = Duration(streamingDurationMs)
    val streamingContext = new StreamingContext(sparkSession.sparkContext, duration)
    checkpointDir.foreach( streamingContext.checkpoint(_))
    streamingContext
  }

  private def streamingFromText(directory: String): DStream[String] =
    ssc.textFileStream(directory)

  private def streamingFromKafka(
    streamFromKafka: StreamFromKafka,
    process: InputDStream[ConsumerRecord[String, String]] => Unit): InputDStream[ConsumerRecord[String, String]]  = {

    val inputStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](streamFromKafka.topics, streamFromKafka.kafkaParams)
    )
    process(inputStream)
    inputStream
  }

  def apply(process: InputDStream[ConsumerRecord[String, String]] => Unit): Unit = {
    val inputStream = streamType match {
      case kafkaStreamType: StreamFromKafka => streamingFromKafka(kafkaStreamType, process)
      case _ => throw new UnsupportedOperationException(s"Stream type ${streamType.toString} not supported")
    }

    inputStream.foreachRDD { rdd =>
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      inputStream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
