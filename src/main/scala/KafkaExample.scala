
import java.util
import java.util.HashMap
import java.util.Properties

import KafkaExample.Sentiment.Sentiment
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import twitter4j.Status
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.io.Source
import scala.collection.convert.wrapAll._


object KafkaExample {
  def main(args: Array[String]): Unit = {

    System.setProperty("twitter4j.oauth.consumerKey", "iB7u0oDnMOQk9ZdW8JWO0SrS4")
    System.setProperty("twitter4j.oauth.consumerSecret", "URt0r8Y4wk2wPKJ0q6CJLSpA6SGj0xytrEWzU3ixHG1yN7Rbna")
    System.setProperty("twitter4j.oauth.accessToken", "1250526509099560963-s2kMaTZeX0bYqiFJIQRNv0DnxCO6wW")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "tXDWXukzPKmMfxE0DZSOMIKOfuNUCskXs7sgGxr7gXSxl")

    if (args.length < 2) {
      println("Usage: topic parameter missing")
      return
    }

    // topic
    val topic = args(0).toString
    val filter = args.slice(1, args.length)

    val kafkaBroker = "localhost:9092"

    // spark configuration
    val sparkConf: SparkConf = new SparkConf().setAppName("spark-kafka-twitter-example")
    val sparkContext = new SparkContext(sparkConf)
    sparkContext.setLogLevel("ERROR")

    // Streaming Context for twitter
    val streamingContext = new StreamingContext(sparkContext, Seconds(5))

    // get the tweets
    val tweets: DStream[(String, Seq[String])] = TwitterUtils.createStream(streamingContext, None, filter)
      .map(_.getText)
      .map(t => (t, t.split(" ")))

    // analyze the sentiment
    def sentiment(input: String): Sentiment = Option(input) match {
      case Some(x) if !x.isEmpty => extractSentiment(x)
      case _ => throw new IllegalArgumentException("input topic can not be empty")
    }

    def extractSentiment(input: String): Sentiment = {
      val (_, sentiment) = extractSentiments(input).maxBy { case (sentence, _) => sentence.length }
      sentiment
    }

    def extractSentiments(str: String): List[(String, Sentiment)] = {
      val properties = new Properties()
      properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
      val p = new StanfordCoreNLP(properties)

      val annotation: Annotation = p.process(str)

      val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])

      sentences.map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
        .map { case (sentence, tree) => (sentence.toString, Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
        .toList
    }

    // process the tweets
    tweets.foreachRDD(rdd => {
      rdd.foreachPartition(partition => {
        val props = new util.HashMap[String, Object]()

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

        val producer = new KafkaProducer[String, String](props)
        partition.foreach(record => {
          val data: String = record.toString()

          val tweet: String = data.split(",")(0)
          val evaluation: Sentiment = sentiment(tweet)


          val msg = new ProducerRecord[String, String](topic, null, evaluation.toString)
          producer.send(msg)

        })
        producer.close()

      })
    })

    streamingContext.start()
    streamingContext.awaitTermination()

  }

  object Sentiment extends Enumeration {
    type Sentiment = Value
    val POSITIVE, NEGATIVE, NEUTRAL = Value

    def toSentiment(sentiment: Int): Sentiment = sentiment match {
      case x if x == 0 || x == 1 => Sentiment.NEGATIVE
      case 2 => Sentiment.NEUTRAL
      case x if x == 3 || x == 4 => Sentiment.POSITIVE
    }
  }

}
