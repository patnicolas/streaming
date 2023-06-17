package org.streamingeval.kafka.streams

import org.scalatest.flatspec.AnyFlatSpec
import org.streamingeval.{RequestPayload, ResponsePayload}

final class RequestsStreamsTest extends AnyFlatSpec{

  it should "Succeed processing a streaming request" in {
    val proc: RequestPayload => ResponsePayload =
      (reqPayload: RequestPayload) => {
        val response = s"${reqPayload.consumedPayload}_produced"
        ResponsePayload(reqPayload.id, response)
      }

    val requestTopic = "test-requests"
    val responseTopic = "test-responses"
    val requestsStreams = new RequestsStreams(proc)
    requestsStreams.start(requestTopic, responseTopic)
  }
}
