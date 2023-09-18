package org.nosql

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID
import java.util.UUID.randomUUID



private[nosql] trait MongoDesc {
  val fileId: UUID
  val source: String
  val path: String
  val fileType: String
  val createdAt: ZonedDateTime
  val contentLength: Long
}


case class MongoFile (
  override val fileId: UUID,
  override val source: String,
  override val path: String,
  override val fileType: String,
  override val createdAt: ZonedDateTime,
  override val contentLength: Long) extends MongoDesc

private[nosql] object MongoFile {
  def apply(
    source: String,
    path: String,
    fileType: String,
    createdAt: ZonedDateTime,
    contentLength: Long): MongoFile =
    MongoFile(randomUUID(), source, path, fileType, createdAt, contentLength)

  def apply(source: String, path: String, fileType: String, contentLength: Long): MongoFile =
    MongoFile(
      randomUUID(),
      source,
      path,
      fileType,
      LocalDateTime.now().atZone(ZoneId.systemDefault()),
      contentLength
    )
}


