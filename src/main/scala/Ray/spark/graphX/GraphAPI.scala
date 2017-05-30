package Ray.spark.graphX

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

/**
  * Created by Ray on 2017/5/26.
  * Usage of basic property of graph.
  */
object GraphAPI {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession.builder()
      .appName("GraphX API Test - Base")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    /**
      * graph:
      *
      *     A       C
      *    /|      /|\
      *   1 |     / | 3
      *  /  |    /  |  \
      * D   7   4   8   F
      *  \  |  /    |  /
      *   2 | /     | 3
      *    \|/      |/
      *     B---2---E
      */

    // vertex of graph
    val vertexArray = Array(
      (1L, ("A", 28)),
      (2L, ("B", 27)),
      (3L, ("C", 65)),
      (4L, ("D", 42)),
      (5L, ("E", 55)),
      (6L, ("F", 50))
    )

    // edge of graph
    val edgeArray = Array(
      Edge(2L, 1L, 7),
      Edge(2L, 4L, 2),
      Edge(3L, 2L, 4),
      Edge(3L, 6L, 3),
      Edge(4L, 1L, 1),
      Edge(5L, 2L, 2),
      Edge(5L, 3L, 8),
      Edge(5L, 6L, 3)
    )

    val vertexRDD: RDD[(Long, (String, Int))] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[(Edge[Int])] = sc.parallelize(edgeArray)

    val graph: Graph[(String, Int), Int] = Graph(vertexRDD, edgeRDD)

    println("graph : ")
    graph.triplets.foreach(println)
    // Information about the Graph
    println("graph.numEdges : " + graph.numEdges)
    println("graph.numVertices " + graph.numVertices)
    println("graph.degrees : " + graph.degrees.collect().mkString("[", ",", "]"))
    println("graph.inDegrees : " + graph.inDegrees.collect().mkString("[", ",", "]"))
    println("graph.outDegrees : " + graph.outDegrees.collect().mkString("[", ",", "]"))

    // Views of the graph as collections
    println("graph.vertices : " + graph.vertices.collect().mkString("[", ",", "]"))
    println("graph.edges : " + graph.edges.collect().mkString("[", ",", "]"))
    println("graph.vertices : " + graph.vertices.collect().mkString("[", ",", "]"))

    // Functions for caching graphs
    graph.persist(StorageLevel.MEMORY_ONLY)
    graph.cache()
    graph.unpersist(false)  // default true
    graph.unpersistVertices(false)  // default true

    spark.close()

  }

}
