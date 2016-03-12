package com.umkc.rode


import java.net.InetAddress
import java.nio.file.{Files, Paths}

import com.umkc.rode.NLPUtils._
import com.umkc.rode.Utils._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * Created by Mayanka on 14-Jul-15.
 */
object SparkNaiveBayes {

  def main(args: Array[String]) {

    System.setProperty("hadoop.home.dir", "D:\\winutils")
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkNaiveBayes").set("spark.driver.memory", "3g").set("spark.executor.memory", "3g")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val sc = ssc.sparkContext
    val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value
    val labelToNumeric = createLabelMap("data2/training/")
    var model: NaiveBayesModel = null
    val PORT_NUMBER = 9999
    // Training the data
    if (!Files.exists(Paths.get("data2/model/NB"))) {

      val training = sc.wholeTextFiles("data2/training/*")
        .map(rawText => createLabeledDocument(rawText, labelToNumeric, stopWords))
      val X_train = tfidfTransformer(training)
      model = NaiveBayes.train(X_train, lambda = 1.0)
      model.save(sc, "data2/model/NB")
    }
    else {
      model = NaiveBayesModel.load(sc, "data2/model/NB")
    }*/

    // Get IP Address of the Machine
    println("IP ADDRESS : :   " + socket.findIpAdd())

    // Socket open for Testing Data
    lazy val address: Array[Byte] = Array(192.toByte, 168.toByte, 1.toByte, 182.toByte)
    val ia = InetAddress.getByAddress(address)

    val lines = ssc.socketTextStream(ia.getHostName, PORT_NUMBER, StorageLevel.MEMORY_ONLY)

    val data = lines.map(line => {
      val test = createLabeledDocumentTest(line, labelToNumeric, stopWords)
      println(test.body)
      test.body
    })

    data.foreachRDD(rdd => {
      val X_test = tfidfTransformerTest(sc, rdd)
      val predictionAndLabel = model.predict(X_test)
      println("PREDICTION")
      predictionAndLabel.foreach(x => {
        labelToNumeric.foreach { y => if (y._2 == x) {
          println(y._1)
          socket.sendCommandToRobot(y._1)

        }
        }
      })


    }
    )

    ssc.start()
    ssc.awaitTermination()








  }


}
