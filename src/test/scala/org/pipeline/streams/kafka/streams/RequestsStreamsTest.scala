package org.pipeline.streams.kafka.streams

import org.scalatest.flatspec.AnyFlatSpec
import org.pipeline.streams.{RequestPayload, ResponsePayload}

private[kafka] final class RequestsStreamsTest extends AnyFlatSpec{
  import RequestsStreamsTest._
  

  it should "Succeed processing a simple streaming request" in {
    val requestTopic = "test-requests"
    val responseTopic = "test-responses"

    val requestsStreams = new RequestsStreams(simpleProc)
    requestsStreams.start(
      requestTopic,
      responseTopic
    )
  }

  ignore should "Succeed processing a streaming request with delay" in {
    val requestTopic = "test-requests"
    val responseTopic = "test-responses"

    val requestsStreams = new RequestsStreams(simpleProc)
    requestsStreams.start(requestTopic, responseTopic)
  }
}


object RequestsStreamsTest {
  sealed trait Status

  case class Failure(error: String) extends Status
  case object Success extends Status
  case object Unknown extends Status

  def processStatus(status: Status): String = status match {
    case Failure(errorMsg) => errorMsg
    case Success => "Succeeded"
    case Unknown => "Undefined status"
  }

  final val simpleProc: RequestPayload => ResponsePayload =
    (reqPayload: RequestPayload) => {
      val response = s"** ${reqPayload.consumedPayload}_produced"
      println(response)
      ResponsePayload(reqPayload.id, response)
    }


  final val procWithDelay: RequestPayload => ResponsePayload =
    (reqPayload: RequestPayload) => {
      print(s"** Payload: ${reqPayload.toString}")
      val msg = reqPayload.consumedPayload
      val sleepTime: Long = try { msg.toLong }
      catch {
        case e: NumberFormatException =>
          println(s"Payload $msg should be an integer ${e.getMessage}")
          2000L
      }
      try {
          Thread.sleep(sleepTime)
      }
      catch {
        case e: InterruptedException =>
          println(s"Failed to delay the procedure ${e.getMessage}")
      }
      val response = s"Produced after $sleepTime milliseconds"
      println(response)
      ResponsePayload(reqPayload.id, response)
    }
}
