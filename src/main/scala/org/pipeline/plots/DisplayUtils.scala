package org.pipeline.plots

import org.slf4j.Logger

object DisplayUtils {
  private val DEFAULT_SHOW_RETURN = 0 // Default return value after info display
  private val DEFAULT_ERROR_RETURN = -1 // Default return value for error
  private val DEST_CONSOLE = 0x01 // Flag to dump computation results on std out
  private val DEST_LOGGER = 0x02 // Flag to dump computation results into log4j log
  private val DEST_CHART = 0x04 // Flag to plot computation results

  private val LOG_DESTINATION = Map[String, Int](
    "console" -> DEST_CONSOLE,
    "logger" -> DEST_LOGGER,
    "chart" -> DEST_CHART, "none" -> 0x00
  )

  /**
   * Class that extends the Try with a descriptive implementation ofthe getOrElse
   * method
   * @tparam T parameter type for the Try
   * @param _try try instance to be extended
   */
  implicit class extendTry[T](_try: scala.util.Try[T]) {

    /**
     * Extends the semantic of getOrElse by including debugging information
     * @tparam U parameter type for the error or recovery return type
     * @param default  error function
     * @param comment description of the error
     * @param logger instance of logging mechanism
     * @return returned error value
     */
    def error[U >: T](default: => U, comment: String, logger: Logger): U =
      _try.getOrElse({ processError(comment, logger); default })
  }

  /**
   * Class that extends the Option with a descriptive implementation of the getOrElse
   * method
   * @tparam T parameter type for the Try
   * @param _option Option instance to be extended
   * @author Patrick Nicolas
   * @since 0.99
   */
  implicit class extendOption[T](_option: Option[T]) {

    /**
     * Extends the semantic of getOrElse by including debugging information
     * @tparam U parameter type for the error or recovery return type
     * @param default  error function
     * @param comment description of the error
     * @param logger instance of logging mechanism
     * @return returned error value
     */
    def error[U >: T](default: => U, comment: String, logger: Logger): U =
      _option.getOrElse({ processError(comment, logger); default })
  }

  private var destination: Int = DEST_CONSOLE + DEST_CHART

  /**
   * Initialize the display configuration using the argument passed in the command line
   * '''sbt "test:run options'''/
   * @param args command line arguments.
   */
  def init(args: Array[String]): Unit =
    destination = args./:(0)((dest, arg) => dest + LOG_DESTINATION.getOrElse(arg, 0))

  /**
   * Test if plotting of computation results has been enabled
   * @return true if charts have to be displayed, false otherwise
   */
  final def isChart: Boolean = (destination & 0x04) == 0x04

  /**
   * Global function that align a label against a boundary. There is no alignment if the
   * size of the placement is smaller than the actual label.
   * @param label Label to be aligned
   * @param length Length of the box the label has to be aligned
   */
  final def align(label: String, length: Int): String = {
    require(!label.isEmpty, "DisplayUtils.align Label is undefined")
    require(
      length < 128,
      s"DisplayUtils.align Size of label placement ${label.length} is incorrect"
    )

    if (length < label.length)
      label
    else {
      val blankChars = new Array[Char](length - label.length)
      label + new String(blankChars)
    }
  }

  /**
   * Display the value of parameterized type in either standard output,
   * or log4j log or both
   * @param t value to be displayed
   * @param logger Reference to the log4j log appender
   * @param alignment Align the label within its placement if alignment is greater than
   * the label, no alignment otherwise
   * @return 0
   */
  final def show[T](t: T, logger: Logger, alignment: Int = -1): Int = {
    print(if (alignment != -1) align(t.toString, alignment) else t.toString, logger)
    DEFAULT_SHOW_RETURN
  }

  /**
   * Display the sequence of parameterized type in either standard output,
   * or log4j log or both
   * @param seq sequence of values to be displayed
   * @param logger Reference to the log4j log appender
   * @return 0
   */
  final def show[T](seq: Seq[T], logger: Logger): Int = {
    seq.mkString(" ")
    DEFAULT_SHOW_RETURN
  }

  /**
   * Display the error related to the value of a parameterized
   * @param t value to be displayed
   * @param logger Reference to the log4j log appender
   * @return -1
   */
  final def error[T](t: T, logger: Logger): Int = {
    processError(t, logger)
    DEFAULT_ERROR_RETURN
  }

  /**
   * Display the content of an exception related to the value of a parameterized.
   * The stack trace related to the exception is printed
   * @param t value to be displayed
   * @param logger Reference to the log4j log appender
   * @param e Exception caught
   * @return -1
   */
  final def error[T](t: T, logger: Logger, e: Throwable): Int = {
    processError(t, logger, e)
    DEFAULT_ERROR_RETURN
  }

  /**
   * Display the error related to the value of a parameterized.
   * @param t value to be displayed
   * @param logger Reference to the log4j log appender
   * @return None after logging the error message
   */
  final def none[T](t: T, logger: Logger): None.type = none(t, logger)

  /**
   * Display the content of an exception related to the value of a parameterized.
   * The stack trace related to the exception is printed
   * @param t value to be displayed
   * @param logger Reference to the log4j log appender
   * @param e Exception caught
   * @return None after logging the error message
   */
  final def none[T](t: T, logger: Logger, e: Throwable): None.type = {
    processError(t, logger, e)
    None
  }
  final def failure[T](t: T, logger: Logger, e: Throwable): Int = {
    processError(t, logger, e)
    DEFAULT_ERROR_RETURN
  }

  private def processError[T](t: T, logger: Logger, e: Throwable): Unit =
    print(s"Error: ${t.toString} with ${e.toString}", logger)

  private def processError[T](t: T, logger: Logger): Unit =
    print(s"Error: ${t.toString}", logger)

  private def print(msg: String, logger: Logger): Unit = {
    if ((destination & 0x01) == 0x01)
      Console.println(msg)
    if ((destination & 0x02) == 0x02) { logger.error(msg); println("log") }
  }
}
