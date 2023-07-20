package org.streamingeval

import org.streamingeval.kafka.prodcons.TopicsManager.AdminClientState
import org.streamingeval.kafka.prodcons.TypedKafkaGenerator
import org.streamingeval.kafka.streams.RequestsStreams
import org.streamingeval.util.LocalFileUtil

object StreamingEval extends App {

  final val simpleProc: RequestPayload => ResponsePayload = (reqPayload: RequestPayload) => {
    val response = s"${reqPayload.consumedPayload}_produced"
    println(response)
    ResponsePayload(
      reqPayload.id,
      response
    )
  }

  AdminClientState.start()
  val requestTopic = "test-requests"
  produceRequests(requestTopic)
  println(s"Message generated")

  try { Thread.sleep(6000L)}
  catch {
    case e: InterruptedException => println(s"ERROR ${e.getMessage}")
  }

  println(s"Start streaming serv ice")
  val responseTopic = "test-responses"
  val requestsStreams = new RequestsStreams(simpleProc)
  requestsStreams.start(requestTopic, responseTopic)
  AdminClientState.close()

  private def produceRequests(topic: String): Unit = {
    // generate the messages
    val numCycles = 20
    val generatedMessages = generateMessages(numCycles)
    // Produce messages to Kafka
    val kafkaProducer = new TypedKafkaGenerator[RequestMessage](topic)
    kafkaProducer.send(generatedMessages)
  }


  private def generateMessages(numCycles: Int): Seq[(String, RequestMessage)] = {
    val contents = Array[String](
      "input/note1.txt",
      "input/note3.txt",
      "input/note4.txt",
      "input/note3.txt",
      "input/note1.txt"
    ).flatMap(LocalFileUtil.Load.local).map(
      _.replaceAll(
        "\n",
        " "
      ).replaceAll(
        "\r",
        ""
      )
    )
    (0 until numCycles).map(
      index => {
        val relIndex = index % contents.length
        val key = index.toString
        val msg = RequestMessage(
          System.currentTimeMillis(),
          RequestPayload(key, contents(relIndex))
        )
        (key, msg)
      }
    )
  }
}