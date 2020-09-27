
## Set up Kafka environments:
	** Go to the root path of Kafka
	** Start Zookeeper: bin/zookeeper-server-start.sh config/zookeeper.properties
	** Start Kafka: bin/kafka-server-start.sh config/server.properties
	** Create a topic: bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic [topicName]
	** Start a consumer: bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic [topicName]
## Set up ELK
	1) Start elasticsearch:	
		1. Go to the root path of elasticsearch
		2. Switch to the user that is not the root
		3. Run ./bin/elasticsearch
	2) Start Kibana
		1. Go to the root path of Kibana
		2. Run ./bin/kibana --allow-root
	3) Start logstash
		1. Write the logstash-simple.conf as told in the instruction
		2. Go to the root path of logstash
		3. ./bin/logstash -f logstash-simple.conf
## Run the jar file
	1) Go to the folder with the Sentiment-assembly-0.1.jar file.
	2) Run the following command； spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class KafkaExample Sentiment-assembly-0.1.jar [topicName] [topicName]
	3）Jar file can be downloaded : https://bigdata-assign3-p1.s3.amazonaws.com/Sentiment-assembly-0.1.jar
## Visualize
	1) Open http://localhost:5601 in browser
	2) Create the index pattern for the [topicName]
	3) Choose the charts you want to visualize the trend.
