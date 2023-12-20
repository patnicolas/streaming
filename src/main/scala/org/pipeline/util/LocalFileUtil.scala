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
package org.pipeline.util

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala._
import java.io._
import java.nio.charset.CodingErrorAction
import java.util.regex.PatternSyntaxException
import org.apache.spark.sql._
import org.slf4j._
import scala.collection.mutable.ListBuffer
import scala.io._
import scala.reflect.ClassTag

/**
 * Generic routine for Loading and Saving content into in local file or HDFS
 * @note scala.io.Source may throw an NIO charset exception if some of the character in a
 *       file does not meet strict UTF-8 standard. In this case, a decoder has to be added
 *       as curried argument.
 * @author Patrick Nicolas
 * @version 0.0.1
 */
private[pipeline] object LocalFileUtil {
  import scala.reflect.runtime.universe.TypeTag

  val logger: Logger = LoggerFactory.getLogger("LocalFileUtil")
  final val CSV_SEPARATOR = ","

  final private val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.IGNORE)


  /**
   * Name space for loading content from rile
   */
  final object Load {
    /**
     * Load resources as stream
     *
     * @param fsResourceFileName Name of the local resource file
     * @return Optional content of the resources
     */
    def resource(fsResourceFileName: String): Option[String] = {
      import scala.io.Source._

      var source: Source = null
      try {
        val inputStream = this.getClass.getResourceAsStream(fsResourceFileName)
        if (inputStream != null) {
          source = fromInputStream(inputStream)(decoder)
          val lines: Iterator[String] = source.getLines()

          val collector = new ListBuffer[String]()
          while (lines.hasNext)
            collector.append(lines.next())
          Some(collector.mkString("\n"))
        }
        else
          None
      }
      catch {
        case e: IOException =>
          logger.error(s"Load $fsResourceFileName with ${e.getMessage}")
          None
      }
      finally {
        try {
          if (source != null)
            source.close
        }
        catch {
          case e: IOException =>
            logger.error(s"Load $fsResourceFileName with ${e.getMessage}")
            None
        }
      }
    }

    /**
     * Loading a file from local file system
     *
     * @param fsFilename Name of file from which to load the content
     * @return
     */
    def local(fsFilename: String): Option[String] = {
      import scala.io.Source._

      var source: Source = null
      try {
        source = fromFile(fsFilename)(decoder)
        val lines: Iterator[String] = source.getLines()
        val collector = new ListBuffer[String]()
        var count = 0

        while (lines.hasNext) {
          val next = lines.next().trim()
          collector.append(next)
          count += 1
          if (count % 2500 == 0)
            logger.info(s"$count records have been loaded")
        }
        Some(collector.mkString("\n"))
      }
      catch {
        case e: IOException =>
          logger.error(s"$fsFilename with ${e.getMessage}")
          None
        case e: Exception =>
          logger.error(s"$fsFilename with ${e.getMessage}")
          None
      }
      finally {
        try {
          if (source != null) {
            try { source.close() }
            catch { case e: IOException => logger.error(s"Failed to close $fsFilename") }
          }
        }
        catch {
          case e: IOException =>
            logger.error(s"$fsFilename with ${e.getMessage}")
        }
      }
    }

    def local[T: ClassTag](fsFilename: String, parse: String => T): Option[Array[T]] = local[T](fsFilename, parse, true)

    def local[T: ClassTag](fsFilename: String, parse: String => T, header: Boolean): Option[Array[T]] =
      if (header)
        local[T](fsFilename, parse, 1, Int.MaxValue)
      else
        local[T](fsFilename, parse, 0, Int.MaxValue)

    /**
     *
     * @param fsFilename Name of the file
     * @param parse      Parser for a line entry in the file
     * @param from       starting index
     * @param to         Ending index
     * @return Optional sequence of keys or values
     */
    def local[T: ClassTag](
      fsFilename: String,
      parse: String => T,
      from: Int,
      to: Int,
      filter: Option[String => Boolean] = None): Option[Array[T]] = {
      require(to >= from, s"Loading window [$from, $to] is out of range")
      import scala.collection.mutable.ListBuffer
      import scala.io.Source._


      def loadingWindow(from: Int, to: Int, count: Int): Boolean =
        (from < 0 && to < 0) || (count >= from && count < to)

      var source: Source = null
      try {
        source = fromFile(fsFilename)
        val lines: Iterator[String] = source.getLines()
        var count = 0
        val collector = ListBuffer[T]()
        while (lines.hasNext) {
          val line = lines.next()

          // Accept only records only within the window
          if (filter.forall(_(line)) && loadingWindow(from, to, count)) {
            val parsed: T = parse(line)
            if (parsed == null)
              throw new IllegalStateException(s"Failed to parse $fsFilename")
            collector.append(parsed)
          }
          count += 1
        }
        Some(collector.toArray)
      }
      catch {
        case e: PatternSyntaxException =>
          logger.error(s"$fsFilename ${e.getMessage}")
          None
        case e: IOException =>
          logger.error(s"$fsFilename ${e.getMessage}")
          None
        case e: IllegalStateException =>
          logger.error(s"$fsFilename ${e.getMessage}")
          None
      }
      finally {
        if (source != null) {
          try {
            source.close
          }
          catch {
            case e: IOException =>
              logger.error(s"$fsFilename ${e.getMessage}")
              None
          }
        }
      }
    }

    /**
     * Local extraction of pairs of {content identifier, content}
     *
     * @param fsPath Path or directory containing content
     * @param f      Conversion of a pair [id, content) to a parameterized type
     * @return Optional sequence of pair of {content identifier, content}
     * @note The decoder is needed to avoid nio character set exception
     */
    private def fs[T: ClassTag](fsPath: String, f: (String, String) => T): Option[Array[T]] = {
      import java.io.File
      import scala.io.Source._

      try {
        val dir = new File(fsPath)
        if (dir.isDirectory) {
          val files: Array[File] = dir.listFiles
          fs[T](files, f)
        }
        else {
          var source: scala.io.Source = null
          try {
            source = fromFile(dir)
            val note = source.getLines().mkString("\n")
            val notesSeq = Array[T](f(dir.getName, note))
            Some(notesSeq)
          }
          finally {
            if (source != null) {
              try { source.close }
              catch {
                case e: IOException =>
                  logger.error(s"Failed to close $fsPath")
                  None
              }
            }
          }
        }
      }
      catch {
        case e: IOException =>
          logger.error(e.toString)
          None
      }
    }

    /**
     * Local extraction of pairs of {content identifier, content}
     *
     * @param files sequence of files
     * @param f     Conversion of a pair (identified, content) to a parameterized type
     * @return Optional sequence of pair of {content identifier, content}
     * @note The decoder is needed to avoid nio character set exception
     */
    private def fs[T: ClassTag](files: Array[File], f: (String, String) => T): Option[Array[T]] =
      if (files.nonEmpty) {
        import scala.io.Source._

        var source: scala.io.Source = null
        try {
          if (files.nonEmpty) {
            val notes: Array[T] = files.map(
              file => {
                val fileName = file.getAbsolutePath
                source = fromFile(fileName)(decoder)
                val note = source.getLines().mkString("\n")
                f(file.getName, note)
              }
            )
            Some(notes)
          }
          else
            None
        }
        catch {
          case e: IOException =>
            logger.error(e.toString)
            None
        }
        finally {
          if (source != null) {
            try { source.close }
            catch {
              case e: IOException =>
                logger.error(s"Failed to close")
                None
            }
          }
        }
      }
      else
        Some(Array.empty[T])

    def fs(fsPath: String): Option[Array[(String, String)]] = fs(fsPath, (id: String, note: String) => (id, note))

    def fs(files: Array[File]): Option[Array[(String, String)]] = fs(files, (id: String, note: String) => (id, note))
  }

  /**
   * Namespace for saving, storing data
   */
  final object Save {
    /**
     * Generic save to local file, If a file with the same name exists the
     * new content will be append to the existing one
     *
     * @param fsFileName Name of the file
     * @param content    content to add or append to this file
     * @return true if execution was successful, false otherwise
     */
    def local(fsFileName: String, content: String, append: Boolean = true): Boolean = {
      import java.io.PrintWriter

      var printWriter: PrintWriter = null
      try {
        // If the file exists, append to it
        if ((new File(fsFileName)).exists()) {
          val fileWriter = new FileWriter(fsFileName, append)
          printWriter = new PrintWriter(fileWriter)
        }
        // ... otherwise create a new file
        else
          printWriter = new PrintWriter(fsFileName)
        printWriter.write(content)
        true
      }
      catch {
        case e: IOException =>
          logger.error(s"$fsFileName with ${e.getMessage}")
          false
      }
      finally {
        if (printWriter != null)
          try {
            printWriter.close()
          }
          catch {
            case e: IOException =>
              logger.error(s"$fsFileName with ${e.getMessage}")
          }
      }
    }
  }

  /**
   * Generic Jackson JSON default Scala mapper
   */
  final object Json {
    val mapper: JsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build()
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Load parameterized data from the local JSON file
     *
     * @param fsFileName Name of the JSON file
     * @return Optional instance of parameterized type
     */
    def load[T](fsFileName: String, clazz: Class[T]): Option[T] =
      try {Load.local(fsFileName).map(mapper.readValue(_, clazz))}
      catch {
        case e: JsonParseException =>
          logger.error(s"$fsFileName with ${e.getMessage}")
          None
        case e: JsonMappingException =>
          logger.error(s"$fsFileName with ${e.getMessage}")
          None
        case e: IOException =>
          logger.error(s"$fsFileName with ${e.getMessage}")
          None
      }

    def save[T](fsOutputFileName: String, data: T): Boolean = try {
      val jsonData = mapper.writeValueAsString(data)
      Save.local(fsOutputFileName, jsonData)
      true
    }
    catch {
      case e: IOException =>
        logger.error(s"$fsOutputFileName with ${e.getMessage}")
        false
      case e: Exception =>
        logger.error(s"$fsOutputFileName with ${e.getMessage}")
        false
    }

    def read[T <: Product : TypeTag](
      fsInputFileName: String
    )(implicit sparkSession: SparkSession, encoder: Encoder[T]): Dataset[T] =
      sparkSession.read.schema(Encoders.product[T].schema).json(fsInputFileName).as[T]

    def write[T](
      fsOutputFilename: String,
      data: Dataset[T],
      saveMode: SaveMode = SaveMode.Append
    )(implicit sparkSession: SparkSession, encoder: Encoder[T]): Unit =
      data.write.mode(saveMode).json(fsOutputFilename)

    /**
     * Merge and filter multiple files contained in a directory into a single file
     *
     * @param fsSourceDirectory Source directory
     * @param fsDestination     destination file
     * @param extension         Optional filter
     * @return Optional destination (if files are found and filter is not empty)
     */
    def merge(
      fsSourceDirectory: String, fsDestination: String, extension: Option[String] = None): Option[String] = {
      val dir = new java.io.File(fsSourceDirectory)

      if (dir.isDirectory) {
        val listOfFiles = extension.map(
          ext => {
            val filenameFilter = new java.io.FileFilter {
              override def accept(file: File): Boolean = file.getName.endsWith(ext)
            }

            dir.listFiles(filenameFilter).flatMap(f => Load.local(f.getAbsolutePath))
          }
        ).getOrElse({
          dir.listFiles.flatMap(f => LocalFileUtil.Load.local(f.getAbsolutePath))
        })

        if (listOfFiles.nonEmpty) {
          val listOfFilesIter = listOfFiles.iterator
          while (listOfFilesIter.hasNext) {
            Save.local(fsDestination, listOfFilesIter.next())
          }
          Some(fsDestination)
        }
        else None
      }
      else
        Some(fsDestination)
    }
  }
}
