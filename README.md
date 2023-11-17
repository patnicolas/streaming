### streaming Patrick Nicolas Last update 11.15.2023.


![Evaluation and tips for Kafka and Spark streaming](images/streaming.jpg)


Evaluation of Apache Kafka and Spark streaming functionality in Scala with example for weather 
forecast

## Reference
- [Open Source Lambda Architecture for Deep Learning](http://patricknicolas.blogspot.com/2021/06/open-source-lambda-architecture-for.html)
- [Boost real-time processing with Spark Structured Streaming](https://patricknicolas.blogspot.com/2023/11/boost-real-time-processing-with-spark.html)

## Environment
| Package      | Version |
|:-------------|:--------|
| Scala        | 2.12.15 |
| Apache Spark | 3.4.0   |
| Apache Kafka | 3.4.0   |
| Jackson ser  | 2.13.1  |
| Kubernetes   | 16.0.0  |



## Updates
| Date       | Version |
|:-----------|:--------|
| 01.20.2023 | 0.0.1   |
| 06.03.2023 | 0.0.2   |
| 08.12.2023 | 0.0.3   |
| 10.30.2023 | 0.0.4   |


## Packages
| Package               | Description                                                 |
|:----------------------|:------------------------------------------------------------|
| util                  | Utilities classes                                           |
| kafka                 | Classes related to Kafka service management                 |
| kafka/prodcons        | Classes related to Kafka producer/consumer                  |
| kafka/streams         | Classes related to Kafka streaming                          |
| spark                 | Classes related to spark datasets and structured streaming  |
| spark/etl             | Spark structured streaming for generic ETL                  |
| spark/weatherTracking | Spark structured streaming application for tracking weather |


## Deployment
From dockerfile for local deployment for a root directory 'myhome':    
- Linux X64: **docker --build-args jdk_target=linux_x64 home=myhome build -t streaming**         
- MacOS X64: **docker --build-args jdk_target=macos_x64 home=myhome build -t streaming**
- MacOS ARM: **docker --build-args jdk_target=macos_aarch64 home=myhome build -t streaming**

## Kafka 

### Launch script
<pre>
zookeeper-server-stop
kafka-server-stop
sleep 2
zookeeper-server-start $KAFKA_ROOT/kafka/config/zookeeper.properties &
sleep 1
ps -ef | grep zookeeper
kafka-server-start $KAFKA_ROOT/kafka/config/server.properties &
sleep 1
ps -ef | grep kafka
</pre>



## Spark structured streaming 

### Libraries - pom.xml
<pre>
   Spark version: <spark.version>3.4.0</spark.version>
   <groupId>org.apache.spark</groupId>
   <artifactId>spark-core_2.12</artifactId>

   <groupId>org.apache.spark</groupId>
   <artifactId>spark-sql_2.12</artifactId>

   <groupId>org.apache.spark</groupId>
   <artifactId>spark-streaming_2.12</artifactId>

   <groupId>org.apache.spark</groupId>
   <artifactId>spark-hadoop-cloud_2.12</artifactId>

   <groupId>org.apache.spark</groupId>
   <artifactId>spark-streaming-kafka-0-10_2.12</artifactId>
</pre>

### Command lines application

To list topics for local deployment of Kafka service
<pre>
kafka-topics --bootstrap-server localhost:9092 --list
</pre>

To create a new topic (i.e. doppler) for local deployment of Kafka service
<pre>
kafka-topics
--bootstrap-server localhost:9092
--topic doppler
--create
--replication-factor 1
--partitions 2
</pre>

To list messages for a given topic (i.e., weather) for local deployment of Kafka service
<pre>
kafka-console-consumer
--topic weather
--from-beginning
--bootstrap-server localhost:9092
</pre>

### Use case: Weather forecast
Architecture
![Architecture](images/Kafka-Spark-Streaming.png)




