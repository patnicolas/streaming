package org.streamingeval.kafka.streams

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.{RequestPayload, ResponsePayload}

final class RequestsStreamsTest extends AnyFlatSpec{
  import RequestsStreamsTest._

  it should "Succeed processing a simple streaming request" in {
    val requestTopic = "test-requests"
    val responseTopic = "test-responses"
    val requestsStreams = new RequestsStreams(simpleProc)
    requestsStreams.start(requestTopic, responseTopic)
  }

  it should "Succeed processing a streaming request with delay" in {
    val requestTopic = "test-requests"
    val responseTopic = "test-responses"
    val requestsStreams = new RequestsStreams(simpleProc)
    requestsStreams.start(requestTopic, responseTopic)
  }
}


object RequestsStreamsTest {
  final val simpleProc: RequestPayload => ResponsePayload =
    (reqPayload: RequestPayload) => {
      val response = s"${reqPayload.consumedPayload}_produced"
      ResponsePayload(reqPayload.id, response)
    }


  final val procWithDelay: RequestPayload => ResponsePayload =
    (reqPayload: RequestPayload) => {
      val msg = reqPayload.consumedPayload
      val sleepTime: Long = try { msg.toLong }
      catch {
        case e: NumberFormatException =>
          println(s"Payload $msg should be an integer")
          2000L
      }
      try {
          Thread.sleep(sleepTime)
      }
      catch {
        case e: InterruptedException =>
          println("Failed to delay the procedure")
      }
      val response = s"Produced after ${sleepTime} milliseconds"
      ResponsePayload(reqPayload.id, response)
    }
}
