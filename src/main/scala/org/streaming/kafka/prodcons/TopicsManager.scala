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
package org.streaming.kafka.prodcons

import org.apache.kafka.clients.admin.{AdminClient, ConsumerGroupListing, NewTopic, SupportedVersionRange, TopicListing}
import org.slf4j.{Logger, LoggerFactory}
import org.apache.kafka.common.errors.TimeoutException
import org.streaming.kafka.{KafkaAdminClient, KafkaTopicException}
import org.streaming.kafka.KafkaAdminClient.{consumerProperties, isAlive}

import scala.jdk.CollectionConverters._
import java.util.Properties
import scala.collection._


/**
 * Manager of Kafka topics given a properties
 * @param properties Properties defined from the consumer
 * @author Patrick Nicolas
 * @version 0.0.1
 */
private[streaming] final class TopicsManager private(properties: Properties) {
  import TopicsManager._, AdminClientState._

  def describeTopic(topic: String): String = {
    val javaTopics: java.util.List[String] = Seq[String](topic).asJava
    val describeTopicsResult = get.describeTopics(javaTopics)
    describeTopicsResult.toString
  }

  def describeFeatures: scala.collection.mutable.Map[String, String] = {
    val featureResult = get.describeFeatures
    val supportedFeatures: java.util.Map[String, SupportedVersionRange] = featureResult.featureMetadata().get.supportedFeatures
    val scalaSupportedFeatures: scala.collection.mutable.Map[String, SupportedVersionRange] = supportedFeatures.asScala
    scalaSupportedFeatures.map{ case (feature, value) => (feature, value.toString)}
  }


  def listConsumerGroupIds: Seq[String] = {
    val consumerGroups: java.util.Collection[ConsumerGroupListing] = get.listConsumerGroups.all.get
    val groups: scala.collection.Iterable[ConsumerGroupListing] = consumerGroups.asScala
    groups.map(_.groupId).toSeq
  }


  /**
   * List the current topics associated with this consumer with self created admin client
   * @return List of topics for this consumer
   */
  def listTopics: Set[String] = try
    if (isAlive(get)) {
      val topicNames = get.listTopics().names().get()
      val topics = topicNames.asScala
      close()
      topics
    }
    else
      throw new KafkaTopicException(s"Cannot list topics Kafka service not running")
  catch {
    case e: TimeoutException =>
      throw new KafkaTopicException(s"List of topics time out: ${e.getMessage}")
    case e: Exception =>
      throw new KafkaTopicException(s"List of topics undefined exception ${e.getMessage}")
  }

  /**
   * Create a new topic, if it does not exist, yet
   * @param topic Name of the topic to create
   * @param adminClient Admin client used to access the Kafka service
   * @param numPartitions Number of partitions (default 6)
   * @param numReplications Number of replications (default 3)
   * @return Updated list of topics
   */
  def createTopic(
    topic: String,
    adminClient: AdminClient,
    numPartitions: Int,
    numReplications: Short): Iterable[String] = {
    val newTopic = new NewTopic(topic, numPartitions, numReplications)
    val results = adminClient.createTopics(scala.collection.immutable.List[NewTopic](newTopic).asJava)
    if (!results.values.isEmpty) Seq[String](topic) else Seq.empty[String]
  }

  /**
   * Create a new topic, if it does not exist, yet, with a self created admin client
   *
   * @param topic           Name of the topic to create
   * @param numPartitions   Number of partitions (default 6)
   * @param numReplications Number of replications (default 3)
   * @return Updated list of topics
   */
  @throws(clazz = classOf[KafkaTopicException])
  def createTopic(
    topic: String,
    numPartitions: Int = defaultNumPartitions,
    numReplications: Short = defaultNumReplications): Iterable[String] = try {
    if(!isTopicDefined(topic)) {
      val listTopics = this.createTopic(topic, AdminClient.create(properties), numPartitions, numReplications)
      close()
      listTopics
    } else
      throw new KafkaTopicException(s"Cannot create topic $topic already exist")
  }
  catch {
    case e: TimeoutException => throw new KafkaTopicException(s"Create topic - time out: ${e.getMessage}")
    case e: Exception => throw new KafkaTopicException(s"Create topic undefined exception ${e.getMessage}")
  }


  /**
   * Delete a topic from Kafka service
   * @param topic Topic to be removed
   * @return Updated list of topics
   */
  @throws(clazz = classOf[KafkaTopicException])
  def deleteTopic(topic: String): Iterable[String] = try {
    if(isTopicDefined(topic)) {
      get.deleteTopics(Seq[String](topic).asJava)

      val listingsFuture = get.listTopics.listings
      val listings: Seq[TopicListing] = listingsFuture.get.asScala.toSeq
      val topics = listings.map(_.name)
      close()
      topics
    }
    else
      throw new KafkaTopicException(s"Cannot delete topic $topic does not exist")
  } catch {
    case e: TimeoutException => throw new KafkaTopicException(s"Delete topic - time out: ${e.getMessage}")
    case e: Exception => throw new KafkaTopicException(s"Delete topic - undefined exception ${e.getMessage}")
  }

  def isTopicDefined(newTopics: Seq[String]): Boolean =
    if (KafkaAdminClient.isAlive) {
      val topics = listTopics(get)
      val isDefined = newTopics.forall(topics.contains)
      close()
      isDefined
    } else
        false


      // -------------------  Supporting methods ------------------

  private def isTopicDefined(topic: String): Boolean = if (KafkaAdminClient.isAlive) {
    val topics = listTopics(get)
    val isDefined = topics.contains(topic)
    close()
    isDefined
  } else false

  /**
   * List the current topics associated with this consumer
   * @param adminClient : Administrator client used to access the list of topics
   * @return List of topics for this consumer
   */
  private def listTopics(adminClient: AdminClient): Set[String] = try {
    if (isAlive(adminClient)) {
      val topicNames = adminClient.listTopics().names().get()
      val topics = topicNames.asScala
      adminClient.close()
      topics
    }
    else {
      logger.error("Kafka server is not running")
      Set.empty[String]
    }
  }
  catch {
    case e: TimeoutException =>
      logger.error(s"Time out: ${e.getMessage}")
      Set.empty[String]
    case e: Exception =>
      logger.error(s"Undefined exception ${e.getMessage}")
      Set.empty[String]
  }
}


/**
 * Singleton for constructors and default values
 */
private[streaming] object TopicsManager {
  private val logger: Logger = LoggerFactory.getLogger("TopicsManager")

  private val defaultNumPartitions: Int = 2
  private val defaultNumReplications: Short = 3

  object AdminClientState {
    private var adminClient: Option[AdminClient] = None

    final def get: AdminClient = adminClient.get

    def start(): Unit = {
      close()
      adminClient = Some(AdminClient.create(consumerProperties))
    }
    def close(): Unit= {
      adminClient.foreach(_.close())
      adminClient = None
    }
  }


  def apply(properties: Properties): TopicsManager = new TopicsManager(properties)

  def apply(): TopicsManager = new TopicsManager(consumerProperties)
}

