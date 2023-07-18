zookeeper-server-stop
ps -ef | grep zookeeper
kafka-server-stop
ps -ef | grep kafka
sleep 2
zookeeper-server-start /opt/homebrew/opt/kafka/config/zookeeper.properties &
sleep 1
kafka-server-start /opt/homebrew/opt/kafka/config/server.properties &
sleep 1
ps -ef | grep zookeeper
ps -ef | grep kafka
