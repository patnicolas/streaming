FROM scala:2.13

ARG jdk-target
ARG home_dir 
RUN echo "JDK target: ${jdk_target}, Home diretory: ${home_dir}"

SPARK_VERSION=3.3.1
HADOOP_VERSION=3
JAVA_VERSION=11
KAFKA_VERSION=3.4.0

# Download and install appropriate JDK
ENV JAVA_HOME="${home_dir}/java/jdk-${JAVA_VERSION}"
RUN DOWNLOAD_URL="https://download.java.net/java/GA/jdk${JAVA_VERSION}/9/GPL/openjdk-${JAVA_VERSION}_${jdk_target}_bin.tar.gz" \
    && TMP_DIR="$(mktemp -d)" \
    && curl -fL "${DOWNLOAD_URL}" --output "${TMP_DIR}/openjdk-${JAVA_VERSION}_${jdk_target}_bin.tar.gz" \
    && mkdir -p "${JAVA_HOME}" \
    && tar xzf "${TMP_DIR}/openjdk-${JAVA_VERSION}_${jdk_target}_bin.tar.gz" -C "${JAVA_HOME}" --strip-components=1 \
    && rm -rf "${TMP_DIR}" \
    && java --version

# Update Path
ENV PATH=${PATH}:${JAVA_HOME}:${JAVA_HOME}/bin
RUN echo $PATH

# download and install Spark
ENV SPARK_HOME="${home_dir}/spark/$SPARK_VERSION"
RUN DOWNLOAD_URL_SPARK="https://archive.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" \
    && wget --no-verbose -O apache-spark.tgz  "${DOWNLOAD_URL_SPARK}"\
    && mkdir -p $SPARK_HOME \
    && tar -xf apache-spark.tgz -C $SPARK_HOME --strip-components=1 \
    && rm apache-spark.tgz

# Update Path
ENV PATH=$PATH:/$SPARK_HOME/bin:/$SPARK_HOME/sbin
RUN echo $PATH

# Download Kafka and Zookeeper
ENV KAFKA_HOME="${home_dir}/kafka/KAFKA_VERSION"
RUN mkdir $KAFKA_HOME \
    && DOWNLOAD_URL_KAFKA="https://downloads.apache.org/kafka/$KAFKA_VERSION/kafka_2.13-$KAFKA_VERSION.tgz" \
    && curl -s $DOWNLOAD_URL_KAFKA | tar -xvf -C $KAFKA_HOME --strip-components=1

# Update PATH
ENV PATH=$PATH:$KAFKA_HOME/bin
RUN echo $PATH

# Start Zookeeper and Kafka
RUN "sh zookeeper-server-start.sh ../config/zookeeper.properties"
RUN "sh kafka-server-start.sh ../config/server.properties

# Build application
RUN mkdir -p "${home_dir}/streaming" \
    && mvn clean install -DskipTests 

