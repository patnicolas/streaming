package org.nosql

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID
import java.util.UUID.randomUUID

private[nosql] case class MongoFile(
  fileId: UUID,
  source: String,
  path: String,
  fileType: String,
  createdAt: ZonedDateTime,
  contentLength: Long
) {
  override def toString: String = s"${source}/$path.${fileType}"
}

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


