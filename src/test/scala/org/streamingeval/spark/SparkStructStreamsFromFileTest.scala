package org.streamingeval.spark

import org.apache.spark.sql.{Column, DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.streaming.OutputMode
import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.PatientRecord
import org.streamingeval.spark.SparkStructStreams.{SAggregator, STransform}
import org.streamingeval.spark.SparkStructStreamsFromFile.{SAggregator, STransform}
import org.streamingeval.util.LocalFileUtil

import scala.collection.mutable
import scala.util.Random

private[spark] final class SparkStructStreamsFromFileTest extends AnyFlatSpec {
  import SparkStructStreamsFromFileTest._

  ignore should "Succeeds extracting Patient record from text" in {
    val input =
      """
        |InternalRequest(498481_20_A2019800701-4379495949,
        |claimType:         Cx
        |Age:               75
        |Gender:            M
        |Taxonomy:          radiology
        |PoS:               21
        |DoS:               07/27/2020
        |Customer:          CKR
        |Client:            no_client
        |Modality:          DIAGNOSTIC
        |EMR Cpts:          Cpt: 71045, modifiers: 26, icds:
        |Provider:          47
        |Patient:           498481
        |Plan id:           40/7/ckr-all
        |Meta data:
        |,List(\r\n================================================================================\r\n\r\n                              ST JOSEPH HOSPITAL\r\n\r\nPatient Name: XXXXX
        |
        |ne placement.\r\n \r\nCOMPARISON: July 21, 2020.\r\n \r\nFINDINGS: The heart is stable in size . The patient is status post\r\nmedian sternotomy. The new left-sided PICC tip terminates in the SVC.\r\nThere has been no significant interval change in the appearance of the\r\nleft lung with parenchymal opacities and pleural effusion. The right\r\nlung is clear.  There is no pneumothorax . The osseous structures\r\ndemonstrate no acute abnormality.\r\n \r\nIMPRESSION: \r\n1. New left PICC tip terminates in SVC.\r\n2. No significant change in appearance of the left lung.\r\n \r\n \r\n \r\nImages reviewed, interpreted, and dictated by Dr. Butler .\r\nTranscribed by Kathryn Spencer, PA-C.\r\n \r\nI have personally viewed, interpreted and dictated the examination. I\r\nhave read and agree with the above final transcribed report.\r\nTranscriptionist: Self Correct, Radiologist\r\nDictated by: SPENCER, KATHRYN, PA-C\r\nDictation Date: 07/27/2020 15:17\r\nInterpreted & Released By: BUTLER, CARINA L, MD-RAD\r\nReleased Date Time: 07/27/2020 15:35\r\n))
        |
        |""".stripMargin

    val jsonString = extractJson(input)
    println(jsonString)
  }

  ignore should "Succeeds generating JSON records" in {
    import implicits._
    import sparkSession.implicits._

    val records = LocalFileUtil
      .Load
      .local(fsFilename = "input/part-17")
      .map(_.split("InternalRequest").filter(_.nonEmpty).take(30))
      .map(_.map(record => extractJson(record)))

    records.foreach(
      records => {
        val recordDS = records.toSeq.toDS()
        recordDS.write.format("json").mode(SaveMode.Overwrite).save(path = "input-json")
      }
    )
  }

  ignore should "Succeed extracting schema from JSON records" in {
    import implicits._
    val path = "/Users/patricknicolas/dev/streaming/input-json"
    val sparkStructStreamsFromFile = SparkStructStreamsFromFile(path,
      OutputMode.Append,
      outputFormat = "console",
      "temp/parquet")
    println(sparkStructStreamsFromFile.getSchema.toString)
  }

  ignore should "Succeed read from stream with a transform" in {
    import implicits._

    val path = "/Users/patricknicolas/dev/streaming/input-json"

    val sparkStructStreamsFromFile =  SparkStructStreamsFromFile(
      path,
      OutputMode.Append(),
      outputFormat = "json",
      "temp/json",
      myTransform
    )
    sparkStructStreamsFromFile.execute()
  }


  it should "Succeed read from stream with a transform and aggregator" in {
    import implicits._
    val path = "/Users/patricknicolas/dev/streaming/input-json"
    val debug = true
    val outputTable = "avg_age"

    val sparkStructStreamsFromFile = SparkStructStreamsFromFile(
      path,
      OutputMode.Update(),
      outputFormat = "csv",
      outputTable,
      debug,
      myTransform,
      myAggregator
    )
    sparkStructStreamsFromFile.execute()
  }
}




object SparkStructStreamsFromFileTest{
  val noteMarker = ",List("
  val ageMarker = "Age"
  val genderMarker = "Gender"
  val taxonomyMarker = "Taxonomy"
  val emrMarker = "EMR Cpts"
  val validMarkers = Set[String](
    ageMarker,
    genderMarker,
    taxonomyMarker,
    emrMarker
  )

  @throws(clazz = classOf[IllegalStateException]) implicit def mapToPatientRecord(
    index: Long,
    attributes: Map[String, String]
  ): PatientRecord = {
    PatientRecord(
      index.toString,
      attributes.getOrElse(
        ageMarker,
        throw new IllegalStateException("Age not found")
      ).toInt,
      attributes.getOrElse(
        genderMarker,
        throw new IllegalStateException("Gender not found")
      ),
      attributes.getOrElse(
        taxonomyMarker,
        throw new IllegalStateException("Taxonomy not found")
      ),
      attributes.getOrElse(
        emrMarker,
        throw new IllegalStateException("EMR not found")
      ),
      attributes.getOrElse(
        noteMarker,
        throw new IllegalStateException("Note not found")
      )
    )
  }

  def extractJson(doc: String): PatientRecord = {
    val collector = mutable.HashMap[String, String]()
    val markerIndex = doc.indexOf(noteMarker)
    if (markerIndex != -1) {
      val note = doc.substring(
        markerIndex + noteMarker.length,
        doc.length - 2
      )
      collector.put(
        noteMarker,
        note
      )
    }

    val lines = doc.split("\n").filter(_.length > 2)
    val attributesMap = lines.foldLeft(collector)(
      (hMap, line) => {
        val fields = line.split(":")
        if (fields.size > 1) {
          val key = fields.head.trim
          if (validMarkers.contains(key)) {
            val value = fields.tail.mkString(" ").trim
            hMap += ((key, value))
          } else hMap
        } else hMap
      }
    )
    mapToPatientRecord(
      Random.nextLong(),
      attributesMap.toMap
    )
  }


  private def myTransformFunc(
    readDF: DataFrame,
    sqlStatement: String
  )
    (implicit sparkSession: SparkSession): DataFrame = {
    readDF.createOrReplaceTempView("temptable")
    sparkSession.sql(sqlStatement)
  }

  import implicits._

  val myTransform = new STransform(
    Seq[String](
      "age",
      "gender"
    ),
    Seq[String]("age > 18"),
    myTransformFunc,
    "Filter by age"
  )

  def aggrFunc(inputColumn: Column): Column = {
    import org.apache.spark.sql.functions._
    avg(inputColumn)vi
  }

  val myAggregator = new SAggregator(
    new Column("gender"),
    new Column("age"),
    aggrFunc,
    "avg_age"
  )
}
