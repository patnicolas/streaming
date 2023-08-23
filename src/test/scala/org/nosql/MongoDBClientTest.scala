package org.nosql

import org.mongodb.scala.bson
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable.ListBuffer

final class MongoDBClientTest extends AnyFlatSpec {

  it should "Succeed accessing a given MongoDB database" in {
    val mongoClient = MongoDBClient("test-db")
    assert(mongoClient.isReady)
    mongoClient.close()
  }

  it should "Retrieve a collection from database 2" in {
    val mongoClient = MongoDBClient("test-db")
    val records = mongoClient.getDocumentString("test_table")
    println(records.mkString("\n"))
    mongoClient.close()
  }

  it should "Insert a  collection from database" in {
    val mongoClient = MongoDBClient("test-db")

    val entry = """{"name":"Roland", "score":0.78}"""
    val document = Document(entry)
    mongoClient.insert("test_table", document)
    val records = mongoClient.getDocumentString("test_table")
    println(records.mkString("\n"))

    mongoClient.close()
  }
}

object MongoDBClientTest  {

}
