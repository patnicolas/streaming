package org


import org.streamingeval.util.ResourceUtil.getFileFromResourceAsStream

import java.util.Properties


package object streamingeval {

  /**
   * Default entry for architecture (Kafka, Spark) parameters
   *
   * @param key       Native name of the parameter
   * @param value     Typed value of the parameter
   * @param isDynamic Is parameter tunable
   * @param paramType Type of parameter (Int, String, Double,....)
   */
  case class ParameterDefinition(key: String, value: String, isDynamic: Boolean, paramType: String) {
    override def toString: String = s"$key $value ${if (isDynamic) "dynamic" else "static"}, $paramType"
  }

  /**
   * Define tuning parameters
   */
  trait TuningParameters[T <: TuningParameters[T]] {
    def getTunableParams: Seq[ParameterDefinition]
  }


  case class RequestPayload(id: String, consumedPayload: String)
  case class ResponsePayload(id: String, producedPayload: String)


  /**
   * Wrapper for the prediction request
   * @param timestamp        Time stamp the request was created
   * @param requestPayload Prediction request
   */
  case class RequestMessage(timestamp: Long, requestPayload: RequestPayload) {
    override def toString: String = s"Timestamp: $timestamp\n${requestPayload.toString}"
  }

  final object RequestMessage {
    def apply(requestPayload: RequestPayload): RequestMessage = RequestMessage(System.currentTimeMillis(), requestPayload)
  }

  /**
   * Message wrapping the prediction response
   *
   * @param timestamp         Time stamp the response was created
   * @param status            HTTP status
   * @param error             HTTP error descripiton
   * @param responsePayload Prediction response
   */
  case class ResponseMessage(
    timestamp: Long,
    status: Int,
    error: String,
    responsePayload: ResponsePayload)


  final object ResponseMessage {
    def apply(
      status: Int,
      error: String,
      responsePayload: ResponsePayload): ResponseMessage =
      ResponseMessage(System.currentTimeMillis(), status, error, responsePayload)

    def apply(responsePayload: ResponsePayload): ResponseMessage =
      ResponseMessage(System.currentTimeMillis(), 200, "OK", responsePayload)
  }


  private final val applicationPropertiesFile: String = "application.properties"

  final val testPredictRequestTopic = "testPredictRequest"
  final val testPredictResponseTopic = "testPredictResponse"
  final val testFeedbackRequestTopic = "testFeedbackRequest"
  final val testFeedbackResponseTopic = "testFeedbackResponse"
  final val saslJaasConfigLabel = "sasl.jaas.config"

  lazy val initialProperties: Option[Properties] = getFileFromResourceAsStream(applicationPropertiesFile, "=")

}
