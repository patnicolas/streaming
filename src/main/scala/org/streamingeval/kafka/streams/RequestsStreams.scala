package org.streamingeval.kafka.streams

import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.{Consumed, Produced}
import org.streamingeval.kafka.serde.{RequestDeserializer, RequestSerDe, RequestSerializer, ResponseDeserializer, ResponseSerializer}
import org.streamingeval.kafka.stringSerde
import org.streamingeval.{RequestMessage, RequestPayload, ResponseMessage, ResponsePayload}
import org.slf4j.{Logger, LoggerFactory}


/**
 *
 * @param proc Transformation function
 */
private[kafka] final class RequestsStreams(proc: RequestPayload => ResponsePayload)
  extends PipelineStreams[RequestMessage](RequestSerDe.deserializingClass) {
  import RequestsStreams._

  override protected[this] def createTopology(requestTopic: String, responseTopic: String): Option[Topology] = try {
    // Invoke the implicit for Consumed[String, PredictRequestMessage]
    val requestMessages = streamBuilder.stream[String, RequestMessage](requestTopic)

    val responseValues = requestMessages.mapValues(
      requestMessage => {
        val responsePayload = proc(requestMessage.requestPayload)
        ResponseMessage(System.currentTimeMillis(),
          200,
          "",
          responsePayload)
      }
    )
    responseValues.to(responseTopic)
    Some(streamBuilder.build)
  }
  catch {
    case e: Exception =>
      logger.error(e.getMessage)
      None
  }

}


private[streamingeval] object RequestsStreams {
  private val logger: Logger = LoggerFactory.getLogger("RequestsStreams")

  private val requestMessageSerde: Serde[RequestMessage] = Serdes.serdeFrom(
    new RequestSerializer,
    new RequestDeserializer
  )
  implicit val consumed: Consumed[String, RequestMessage] = Consumed.`with`(stringSerde, requestMessageSerde)


  // Generate implicit for produced type associated with the Predict Response message
  private val responseMessageSerde: Serde[ResponseMessage] = Serdes.serdeFrom(
    new ResponseSerializer,
    new ResponseDeserializer
  )
  implicit val produced: Produced[String, ResponseMessage] = Produced.`with`(stringSerde, responseMessageSerde)
}
