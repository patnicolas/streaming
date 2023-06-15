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
  * and limitations under the License.
  */
package org.streamingeval.util

import java.util.Properties
import java.io.{FileNotFoundException, IOException}


/**
 * Interface for managing local resource files
 * @author Patrick Nicolas
 * @version 0.0.1
 */
trait ResourceUtil {
  self =>
  import ResourceUtil._

  /**
    * Retrieve the absolute path of resource file
    * @param fsResourceFileName path relative to the resource folder
    * @return absolute path for a resource file
    */
  final def getFsPath(fsResourceFileName: String): Option[String] = {
    val res = getClass.getResource(fsResourceFileName)
    if (res != null)
      Some(res.getPath)
    else {
      logger.error(s"Cannot getPath for $fsResourceFileName")
      None
    }
  }

  /**
    * Retrieve the absolute path of a tuple of resource files
    * @param fsFileResourceName1 first resource file
    * @param fsFileResourceName2 second resource file
    * @return tuple of absolute paths
    */
  final def getFsPath(fsFileResourceName1: String, fsFileResourceName2: String): Option[(String, String)] = {
    val res1 = getClass.getResource(fsFileResourceName1)
    val res2 = getClass.getResource(fsFileResourceName2)
    if (res1 != null && res2 != null) Some((res1.getPath, res2.getPath))
    else {
      logger.error(s"ERROR: IO, cannot get path for $fsFileResourceName1 or $fsFileResourceName2")
      None
    }
  }

  final def cleanse(
    fsSourceFileName: String,
    fsDestinationFileName: String
  )(implicit cleaner: (String, StringBuilder) => Unit): Unit =
    ResourceUtil.cleanse(fsSourceFileName, fsDestinationFileName)
}

/**
  * Companion object for the resource
  */
private[org] object ResourceUtil {
  import org.slf4j.{Logger, LoggerFactory}
  import scala.util.Try

  val logger: Logger = LoggerFactory.getLogger("ResourceUitl")
  val TestCompleted: Boolean = true

  private val lineSplitter = """\\r\\n"""
  def formatNote(rawNote: String): String = {
    val lines = rawNote.substring(1, rawNote.length - 1).split(lineSplitter)
    lines.mkString("\n")
  }


  def info(message: String): Unit =  logger.info(s"\n$message")
  def warn(message: String): Unit =  logger.warn(s"\n$message")
  def error(message: String): Unit =  logger.error(s"\n$message")

  /**
    * Retrieve the absolute path of resource file
    * @param fsResourceFileName path relative to the resource folder
    * @return absolute path for a resource file
    */
  def getFsRelativePath(fsResourceFileName: String): Try[String] = Try {
    getClass.getResource(fsResourceFileName).getPath
  }


  /**
    * Singleton method to load resource file (defined in src/main/resources) using the following format
    * {{{
    *     [First section in line] [separator] [Remaining section in linet]
    * }}}
    * @param fileName Name of the resource file
    * @param separator Separator used in the string
    * @return Optional properties
    */
  def getFileFromResourceAsStream(fileName: String, separator: String): Option[Properties] = try {
    import java.io.BufferedReader
    import java.io.InputStreamReader
    import java.nio.charset.StandardCharsets
    // Class loader
    val classLoader = getClass.getClassLoader

    // Get the input stream from the resource file
    val inputStream = classLoader.getResourceAsStream(fileName)
    if (inputStream != null) {
      import java.util.Properties
      val streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
      val reader = new BufferedReader(streamReader)
      var line: String = null
      val props = new Properties()
      do {
        line = reader.readLine()
        if (line != null && line.contains(separator)) {
          // We do not assume that there is only one separator in each line
          val separatorIndex = line.indexOf(separator)
          if(separatorIndex != -1) {
            val key = line.substring(0, separatorIndex)
            val value = line.substring(separatorIndex+1)
            props.put(key.trim, value.trim)
          }
          else
            logger.error(s"Could not extract key value from $line for $fileName")
        }
      } while (line != null)
      Some(props)
    } else {
      logger.error(s"Could not load the resource file $fileName")
      None
    }
  }
  catch {
    case e: FileNotFoundException =>
      logger.error(e.getMessage)
      None
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }

  /**
    * Cleanse a incorrectly formatted file into
    * @param fsSourceFileName name of the source file
    * @param fsDestinationFileName name of the destination file
    * @param cleaner Implicit transformation function for file records
    */
  @throws(clazz = classOf[java.io.IOException])
  def cleanse(
    fsSourceFileName: String,
    fsDestinationFileName: String
  )(implicit cleaner: (String, StringBuilder) => Unit): Unit = {
    import java.io.PrintWriter
    var printWriter: PrintWriter = null
    try {
      import scala.io.Source._

      val src = fromFile(fsSourceFileName)
      val lines: Iterator[String] = src.getLines
      val buf = new StringBuilder
      while (lines.hasNext) {
        val line = lines.next
        cleaner(line, buf)
      }
      src.close

      printWriter = new PrintWriter(fsDestinationFileName)
      printWriter.write(buf.toString)
    }
    catch {
      case e: FileNotFoundException =>
        logger.error(s"ERROR: IO, incorrect format ${e.getMessage}")
      case e: IOException =>
        logger.error(s"ERROR: IO, incorrect format ${e.getMessage}")
    }
    finally {
      if (printWriter != null) {
        try (printWriter.close())
        catch {
          case e: IOException =>
            logger.error(s"Cleanse cannot close handle ${e.getMessage}")
        }
      }
    }
  }

}