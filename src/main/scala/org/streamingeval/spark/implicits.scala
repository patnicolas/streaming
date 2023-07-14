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

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.streamingeval.spark.SparkConfiguration.confToSessionFromFile

import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}



object implicits {
  import scala.language.implicitConversions

  final private val logger: Logger = LoggerFactory.getLogger("implicits")

  // implicit val system = ActorSystem("MLOps-Service")
  implicit val sparkSession: SparkSession = confToSessionFromFile

  def close: Unit = {
    logger.info("Closing Spark session!")
    sparkSession.close()
  }

  // ----------- Java type to Scala type collection conversion ---------------

  implicit def arrayOfArray(input: java.util.ArrayList[java.util.ArrayList[java.lang.Double]]): Seq[Array[Double]] =
    input.asScala.map(_.asScala.toArray.map(_.toDouble)).toSeq

  implicit def arrayOfDouble(input: java.util.ArrayList[java.lang.Double]): Seq[Double] =
    input.asScala.map(_.toDouble).toSeq

  implicit def collectionOf[T](input: java.util.Collection[T]): Seq[T] = input.asScala.toSeq

  implicit def listOf[T](input: java.util.List[T]): List[T] = input.asScala.toList

  implicit def arrayOf[T](input: java.util.ArrayList[T]): Seq[T] = input.asScala.toSeq

  implicit def map2Scala[T](input: java.util.Map[String, T]): scala.collection.mutable.Map[String, T] =
    input.asScala.map { case (key, value) => (key, value) }

  implicit def arrayOfString(input: java.util.ArrayList[java.lang.String]): Seq[String] = input.asScala.toSeq

  implicit def collection2ScalaStr(input: java.util.Collection[java.lang.String]): Seq[String] = input.asScala.toSeq

  implicit def collection2Scala[T](input: java.util.Collection[T]): Iterable[T] = input.asScala

  implicit def set2Scala[T](input: java.util.Set[T]): scala.collection.Set[T] = input.asScala.toSet
}
