# Twitter-Sentiment-Analysis-using-Spark-Streaming-and-Kafka

Instructions to run:
It needs 7 arguments to be passed. 4 arguments for twitter to generate oAuth credentials
Arguments to be passed: consumer key, consumer secret key, access token, secret access token, 
twitter topic, kafka topic, checkpoint directory path

Run zookeeper, kafka server and create a topic.
Start a consumer to that will dump messages to standard output

Run Elasticsearch, kibana and logstash to visulaize data in real time.

To run the project, run sparkStreaming.scala class on IntelliJ or create an assembly fat jar file and run the jar file with the below command:

spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0
--class ClassName(sparkStreaming) PathToJarFile -- 6 arguments
