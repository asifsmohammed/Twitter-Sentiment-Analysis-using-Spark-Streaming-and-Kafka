import org.apache.log4j.{Level, Logger}
import java.util.Properties
import scala.collection.convert.wrapAll._
import Sentiment.Sentiment

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter._
import org.apache.spark.SparkConf
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

object sparkStreaming {
  val propsNLP = new Properties()
  propsNLP.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(propsNLP)

  def SentimentOutput(input: String): Sentiment = Option(input) match {
    case Some(text) if !text.isEmpty => extractSentiment(text)
    case _ => throw new IllegalArgumentException("input can't be null or empty")
  }

  private def extractSentiment(text: String): Sentiment = {
    val (_, sentiment) = extractSentiments(text)
      .maxBy { case (sentence, _) => sentence.length }
    sentiment
  }

  def extractSentiments(text: String): List[(String, Sentiment)] = {
    val annotation: Annotation = pipeline.process(text)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    sentences
      .map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
      .map { case (sentence, tree) => (sentence.toString,Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
      .toList
  }

  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: TwitterPopularTags <consumer key> <consumer secret> " +
        "<access token> <access token secret> <twitter topic> <kafka topic> <checkpoint dir")
      System.exit(1)
    }

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)

    val filter = args(4)

    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generate OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf().setAppName("twitterStreaming")

    // check Spark configuration for master URL, set it to local if not configured
    if (!sparkConf.contains("spark.master")) {
      sparkConf.setMaster("local[2]")
    }

    val ssc = new StreamingContext(sparkConf, Seconds(1))
    val stream = TwitterUtils.createStream(ssc, None, filters=Seq(filter))
    val sentences = stream.map(status => status.getText)

    val outSentiment = sentences.map(x=>(x, SentimentOutput(x).toString()))

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringSerializer")

    val topic = args(5)

    outSentiment.foreachRDD { rdd =>
      rdd.foreachPartition { partitionOfRecords =>
        val producer = new KafkaProducer[String, String](props)
        partitionOfRecords.foreach(message => {
          val record = new ProducerRecord[String, String](topic, message.toString, message._2.toString())
          producer.send(record)
        })
        producer.close()
      }
    }

    ssc.checkpoint(args(6))

    ssc.start()
    ssc.awaitTermination()
  }
}