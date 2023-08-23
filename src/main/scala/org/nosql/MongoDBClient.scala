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
package org.nosql

import org.mongodb.scala._
import org.mongodb.scala.Document
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Wrapper for the Scala client to MongoDB
 * @param host Host for the MongoDB service
 * @param port Port for the MongoDB service
 * @param dbName Name of the database
 *
 * @author Patrick Nicolas
 * @version 0.0.1
 */
final class MongoDBClient private (host: String, port: Int, dbName: String) {

  private lazy val mongoDBClient = try {
    val connectionString = s"mongodb://${host}:${port.toString}"
    val _mongoClient = MongoClient(connectionString)
    val _mongoDatabase = _mongoClient.getDatabase(dbName)
    Some((_mongoClient, _mongoDatabase))
  }
  catch {
    case e: MongoClientException =>
      println(s"ERROR: ${e.getMessage}")
      None
  }

  def mongoClient: Option[MongoClient] = mongoDBClient.map(_._1)
  def mongoDatabase: Option[MongoDatabase] = mongoDBClient.map(_._2)

  def isReady: Boolean = mongoDBClient.isDefined

  def close(): Unit = mongoClient.foreach(_.close())


  def getDocument(collectionName: String): Seq[Document] =
      mongoDBClient.map(
        client => {
          val collection = client._2.getCollection(collectionName)
          Await.result(collection.find.toFuture(), Duration.Inf)
        }
      ).getOrElse({
        println(s"WARN: Mongo collection ${collectionName} not found")
        Seq.empty[Document]
      })

  def getDocumentString(collectionName: String): Seq[String] = {
    val documents = getDocument(collectionName)
    if(documents.nonEmpty) {
      documents.foldLeft(ListBuffer[String]())(
        (lst, doc) => {
          val docIterator = doc.iterator
          val strBuf = new StringBuilder()
          while (docIterator.hasNext) {
            val record: (String, bson.BsonValue) = docIterator.next()
            val output = record._2.getBsonType.name match {
              case "STRING" => s"${record._1}:${record._2.asString().getValue}"
              case "DOUBLE" => s"${record._1}:${record._2.asDouble().getValue}"
              case "ARRAY" => s"${record._1}:${record._2.asArray()}"
              case _ => ""
            }
            strBuf.append(output).append(", ")
          }
          lst += strBuf.toString()
        }
      )
    }
    else
      Seq.empty[String]
  }

  def insert(collectionName: String, input: Document): Unit =
    mongoDBClient.map(
      client => {
        val collection = client._2.getCollection(collectionName)
        Await.result(
          collection.insertOne(input).toFuture(),
          Duration.Inf
        )
      }
    ).getOrElse(println(s"WARN: Mongo collection ${collectionName} not found"))
}

object MongoDBClient {
  def apply(host: String, port: Int, dbName: String): MongoDBClient =
    new MongoDBClient(host, port, dbName)

  def apply(host: String, dbName: String): MongoDBClient =
    new MongoDBClient(host, 27017, dbName)

  def apply(dbName: String): MongoDBClient =
    new MongoDBClient("localhost", 27017, dbName)
}
