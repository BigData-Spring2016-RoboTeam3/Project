import java.io.{DataInputStream, File}
import java.net.{InetAddress, Socket, ServerSocket}
import java.util.Random

import com.umkc.rode.socket
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

/**
  * Created by Mayanka on 10-Mar-16.
  */
object SimpleRecommendation {

  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\winutils")
    val conf = new SparkConf().setMaster("local[*]").setAppName("SimpleRecommendation")
      .set("spark.executor.memory", "2g")

    val sc = new SparkContext(conf)

    // load personal ratings

    val myRatings = loadRatings("data/personalRating.txt")
    print(myRatings.mkString("  "))
    val myRatingsRDD = sc.parallelize(myRatings, 1)

    // load ratings and movie titles

    val movieLensHomeDir = "data"

    val ratings = sc.textFile(new File(movieLensHomeDir, "ratings.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }

    val training = ratings.union(myRatingsRDD)
      .repartition(4)
      .cache()
    val movies = sc.textFile(new File(movieLensHomeDir, "movies.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect().toMap


    // Build the recommendation model using ALS
    val rank = 12
    val numIterations = 20
    val model = ALS.train(training, rank, numIterations, 0.1)

    val myRatedMovieIds = myRatings.map(_.product).toSet
    val candidates = sc.parallelize(movies.keys.filter(!myRatedMovieIds.contains(_)).toSeq)

    val recommendations = model.predict(candidates.map((0, _))).collect()
    var track:Array[String] = new Array[String](90)
    var index = 0
    var i = 1
    println("Movies recommended for you:")
    recommendations.foreach { r =>
      println(r)
      println("%2d".format(i) + ": " + movies(r.product))
      track(index)= movies(r.product)
      index=index+1

      i += 1
    }

    // clean up
    sc.stop()
    val ss = new ServerSocket(9994)
    val s1: Socket = ss.accept
    System.out.println(ss)
    val din = new DataInputStream(s1.getInputStream)
    val op = din.readLine
    if(op.equals("happy")) {
      val r = new Random()
      val z = r.nextInt(90)
      socket.sendCommandToRobot(track(z))
    }
    //socket.sendCommandToRobot(track(40))
  }

  def loadRatings(path: String): Seq[Rating] = {
    val lines = Source.fromFile(path).getLines()
    val ratings = lines.map { line =>
      val fields = line.split("::")
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.filter(_.rating > 0.0)
    if (ratings.isEmpty) {
      sys.error("No ratings provided.")
    } else {
      ratings.toSeq
    }
  }

}
