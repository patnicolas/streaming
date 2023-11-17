# Batch to launch Zookeeper and Kafka services
zookeeper-server-stop
ps -ef | grep zookeeper
kafka-server-stop
ps -ef | grep kafka
sleep 2
# It is assumed that Zookeeper used the default port 2132 for local host deployment
zookeeper-server-start /opt/homebrew/opt/kafka/config/zookeeper.properties &
sleep 1
ps -ef | grep zookeeper
# It is assumed that Zookeeper used the default port 9092 for local host deployment
kafka-server-start /opt/homebrew/opt/kafka/config/server.properties &
sleep 1
ps -ef | grep kafka


