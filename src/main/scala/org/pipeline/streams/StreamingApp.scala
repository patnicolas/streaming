/**
 * Copyright 2022,2024 Patrick R. Nicolas. All Rights Reserved.
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
package org.pipeline.streams

import org.pipeline.streams.kafka.prodcons.TopicsManager.AdminClientState
import org.pipeline.streams.kafka.prodcons.TypedKafkaGenerator
import org.pipeline.streams.kafka.streams.RequestsStreams
import org.pipeline.util.LocalFileUtil

object StreamingApp extends App {
  val b: String = b + b // de-sugared as val b = (new StringBuilder()).append(null).append(null)
  // .toString()   => "nullnull"   (JVM)
  print(b.length)    // Print a8
  val a: String = a; // Failed because infinite recursion
  println(a.length)

/*
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
    ).flatMap(LocalFileUtil.Load.local(_)).map(
      _.replaceAll("\n", " ").replaceAll("\r", "")
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

 */
}