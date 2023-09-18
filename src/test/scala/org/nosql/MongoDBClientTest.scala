package org.nosql

import org.mongodb.scala.bson
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

final class MongoDBClientTest extends AnyFlatSpec {

  ignore should "Succeed accessing a given MongoDB database" in {
    val mongoClient = MongoDBClient("test-db", false)
    assert(mongoClient.isReady)
    mongoClient.close()
  }

  ignore should "Retrieve a collection from database 2" in {
    val mongoClient = MongoDBClient("test-db",false)
    val records = mongoClient.getDocumentString("test_table")
    println(records.mkString("\n"))
    mongoClient.close()
  }

  ignore should "Insert a  collection from database" in {
    val mongoClient = MongoDBClient("test-db", false)

    val entry = """{"name":"Roland", "score":0.78}"""
    val document = Document(entry)
    mongoClient.insert("test_table", document)
    val records = mongoClient.getDocumentString("test_table")
    println(records.mkString("\n"))

    mongoClient.close()
  }

  it should "Extract mongo collection file" in {
    val mongoClient = MongoDBClient("test-db", true)
    val files = mongoClient.getCollectionFiles("test_table")
    assert(files.isDefined, "Mongo DB files not found")
    val filenames = files.map(_.map(_.toString)).getOrElse({
      assert( false, "Failed to load MongoDB collection files")
      Seq.empty[String]
    })
    println(s"Collection file names: ${filenames.mkString("\n")}")
  }
}

