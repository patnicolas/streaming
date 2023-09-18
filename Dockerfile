FROM openjdk:11
MAINTAINER Patrick Nicolas <pnicolas57@yahoo.com>

ARG home_dir 
RUN echo "Java home directory: ${home_dir}"
ENV JAVA_VERSION 11
ENV SCALA_VERSION 2.13.11
ENV SCALA_DIR /Users/patricknicolas/libraries/
RUN echo Scala directory: ${SCALA_DIR}
# Download and install appropriate JDK
ENV JAVA_HOME=${home_dir}/java/jdk-${JAVA_VERSION}
RUN echo Java Home: ${JAVA_HOME}
ENV SCALA_TARBALL http://www.scala-lang.org/files/archive/scala-$SCALA_VERSION.zip

WORKDIR ${SCALA_DIR}
RUN curl -fL ${SCALA_TARBALL} --output  scala-${SCALA_VERSION}.zip
RUN unzip scala-$SCALA_VERSION.zip
ENV SCALA_HOME ${SCALA_DIR}

# Update Path
ENV PATH=${PATH}:${JAVA_HOME}:${JAVA_HOME}/bin
RUN echo $PATH

# download and install Spark
ENV SPARK_HOME="${home_dir}/spark/$SPARK_VERSION"

# Update Path
ENV PATH=$PATH:/$SPARK_HOME/bin:/$SPARK_HOME/sbin
RUN echo $PATH

# Download Kafka and Zookeeper
ENV KAFKA_HOME="${home_dir}/kafka/$KAFKA_VERSION"

# Update PATH
ENV PATH=$PATH:$KAFKA_HOME/bin
RUN echo $PATH

# Start Zookeeper and Kafka
WORKDIR /opt/homebrew/opt/kafka/bin
RUN sh zookeeper-server-start.sh ../config/zookeeper.properties &
RUN sh kafka-server-start.sh ../config/server.properties &
RUN  /opt/homebrew/opt/mongodb-community/bin/mongod --config /opt/homebrew/etc/mongod.conf &

# Build application
RUN mkdir -p "${home_dir}/streaming" \
WORKDIR  ${home_dir}/streaming"
RUN /opt/homebrew/bin/mvn clean install -DskipTests


