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
package org.pipeline.nosql

import com.mongodb.{ServerApi, ServerApiVersion}
import org.bson.BsonType
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala._
import org.mongodb.scala.Document
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Wrapper for the Scala client to MongoDB
 * @param host Host for the MongoDB service
 * @param port Port for the MongoDB service
 * @param dbName Name of the database
 * @param isFromFile Use mongo file input
 *
 * @author Patrick Nicolas
 * @version 0.0.1
 */
final class MongoDBClient[T <: MongoDesc] private (host: String, port: Int, dbName: String,
  isFromFile: Boolean) {
  import MongoDBClient._

  private lazy val mongoDBClient = try {
    val connectionString = s"mongodb://${host}:${port.toString}"

    // Step 1: Full definition of Mongo client with connection and server API settings
    val serverApi = ServerApi.builder.version(ServerApiVersion.V1).build()
    val settings = MongoClientSettings.builder()
      .applyConnectionString(ConnectionString(connectionString))
      .serverApi(serverApi)
      .build()
    val _mongoClient = MongoClient(settings)

    // Step 2: Instantiation of mongo database using custom CODE
    val _mongoDatabase =
      if(isFromFile) {
        val customCodecs: CodecRegistry = fromProviders(classOf[MongoDesc])
        val codecRegistry = fromRegistries(
          customCodecs,
          DEFAULT_CODEC_REGISTRY
        )
        _mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry)
      }
      else
        _mongoClient.getDatabase(dbName)
    Some((_mongoClient, _mongoDatabase))
  }
  catch {
    case e: MongoClientException =>
      println(s"ERROR: ${e.getMessage}")
      None
  }

  def getCollectionFile(collectionName: String): Option[MongoCollection[MongoFile]] =
    mongoDatabase.map(_.getCollection[MongoFile](collectionName))

  def getCollectionFiles(collectionName: String): Option[Seq[MongoFile]] = try {
    getCollectionFile(collectionName).map(
      mongoCollectionFile => {
        // Asynchronously wait for the
        Await.result(
          mongoCollectionFile.find.toFuture(),
          Duration.Inf
        )
      }
    )
  }
  catch {
    case e: Exception =>
      logger.error(s"ERROR: ${e.getMessage}")
      None
  }

  def mongoClient: Option[MongoClient] = mongoDBClient.map(_._1)
  def mongoDatabase: Option[MongoDatabase] = mongoDBClient.map(_._2)

  def isReady: Boolean = mongoDBClient.isDefined

  def close(): Unit = mongoClient.foreach(_.close())

  def getDocument(collectionName: String): Seq[Document] =
      mongoDBClient.map {
        case  (_, db) =>
          val collection = db.getCollection(collectionName)
          Await.result(collection.find.toFuture(),  Duration.Inf)
      }.getOrElse({
        logger.warn(s"WARN: Mongo collection ${collectionName} not found")
        Seq.empty[Document]
      })

  def getDocumentString(collectionName: String): Seq[String] = {
    val documents = getDocument(collectionName)
    if(documents.nonEmpty) {
      // Accumulate the the
      documents.foldLeft(ListBuffer[String]())(
        (accumulator, document) => {
          val docIterator = document.iterator
          val strBuf = new StringBuilder()

          while (docIterator.hasNext) {
            val record: (String, bson.BsonValue) = docIterator.next()
            val output = record._2.getBsonType match {
              case BsonType.STRING => s"${record._1}:${record._2.asString().getValue}"
              case BsonType.DOUBLE => s"${record._1}:${record._2.asDouble().getValue}"
              case BsonType.ARRAY => s"${record._1}:${record._2.asArray()}"
              case BsonType.DOCUMENT => s"${record._1}:${record._2.asDocument().toString}"
              case BsonType.INT32 =>  s"${record._1}:${record._2.asInt32().getValue}"
              case BsonType.INT64 =>  s"${record._1}:${record._2.asInt64().getValue}"
              case BsonType.BOOLEAN => s"${record._1}:${record._2.asBoolean().getValue}"
              case BsonType.BINARY => s"${record._1}:${record._2.asBinary()}"
              case BsonType.TIMESTAMP => s"${record._1}:${record._2.asTimestamp().getValue}"
              case BsonType.DATE_TIME => s"${record._1}:${record._2.asDateTime().getValue}"
              case BsonType.REGULAR_EXPRESSION => s"${record._1}:${record._2.asRegularExpression().toString}"
              case _ => ""
            }
            strBuf.append(output).append(", ")
          }
          accumulator += strBuf.toString()
        }
      )
    }
    else {
      logger.warn(s"Mongo collection $collectionName is empty")
      Seq.empty[String]
    }
  }

  def insert(source: String, filePath: String, fileType: String, contentLength: Long): Unit =
    if(isFromFile) {
      val mongoFile = MongoFile(source, filePath, fileType, contentLength)
      mongoDBClient.map(
        client => {
          val collectionFile = client._2.getCollection[MongoFile]("file")
          Await.result(
            collectionFile.insertOne(mongoFile).toFuture(),
            Duration.Inf
          )
        }
      )
    }
    else
      throw new IllegalStateException("MongoDBClient is not initialized to support mongo fies")

  def insert(collectionName: String, input: Document): Unit =
    mongoDBClient.map(
      client => {
        val collection = client._2.getCollection(collectionName)
        Await.result(
          collection.insertOne(input).toFuture(),
          Duration.Inf
        )
      }
    ).getOrElse(logger.warn(s"Mongo collection ${collectionName} not found"))
}

object MongoDBClient {
  final private val logger: Logger = LoggerFactory.getLogger("MongoDBClient")

  def apply[T <: MongoDesc] (host: String, port: Int, dbName: String, isFromFile: Boolean)
  : MongoDBClient[T] =
    new MongoDBClient[T](host, port, dbName, isFromFile)

  def apply[T <: MongoDesc] (host: String, dbName: String, isFromFile: Boolean): MongoDBClient[T] =
    new MongoDBClient[T](host, 27017, dbName, isFromFile)

  def apply[T <: MongoDesc](dbName: String, isFromFile: Boolean): MongoDBClient [T]=
    new MongoDBClient[T]("localhost", 27017, dbName, isFromFile)
}
