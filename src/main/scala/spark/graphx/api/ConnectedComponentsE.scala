package spark.graphx.api

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/5/31.
  * Find connected components in graph.
  */
object ConnectedComponentsE {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession.builder()
      .appName("connected components")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    // vertex of graph
    val vertexArray = Array(
      (1L, 0.0),
      (2L, 0.0),
      (3L, 0.0),
      (4L, 0.0),
      (5L, 0.0),
      (6L, 0.0),
      (7L, 0.0)
    )

    // edge of graph
    val edgeArray = Array(
      Edge(1L, 2L, 0),
      Edge(2L, 3L, 0),
      Edge(2L, 4L, 0),
      Edge(3L, 5L, 0),
      Edge(3L, 6L, 0),
      Edge(4L, 1L, 0),
      Edge(5L, 6L, 0),
      Edge(6L, 3L, 0)
    )

    val vertexRDD: RDD[(VertexId, Double)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[(Edge[Int])] = sc.parallelize(edgeArray)

    // Build the initial Graph
    val graph: Graph[Double, Int] = Graph(vertexRDD, edgeRDD)

    println("\ngraph : ")
    graph.triplets.foreach(println)

    val cc = graph.connectedComponents()
    println("\ncc : ")
    cc.vertices.foreach(println)

    val scc = graph.stronglyConnectedComponents(100)
    println("\nscc : ")
    scc.vertices.foreach(println)

    // stop spark session
    spark.stop()
  }

  /**
    * 检测连通图的目的,是弄清一个图有几个连通部分,以及每个连通部分有多少顶点.这样可以将一个大图分割为多个小图,
    * 并去掉零碎的连通部分,从而可以在多个小子图上,进行更加精细的操作.
    *
    * 若图中任意两个顶点都是连通的，则无向图称为连通图，有向图称为强连通图。
    *
    * connected Components 连通组件
    *   构建连通的子图: 找到图中各个连通的部分,并以各部分中顶点ID最小值,标记该部分的所有顶点,
    *                若有与其他顶点都不连通的顶点,则用自己的ID值标记自己.
    *   该方法不会考虑边的方向,只要两个顶点间有一条路径,就认为两个顶点连通.
    *   该方法中有一个可选的Int 类型的参数 numIter,决定查找计算子图时的迭代次数:
    *       次数太少,会影响子图的发现,
    *       次数太多,影响不是很大,底层调用 Pregel进行迭代计算,当没有消息发送时也会停止迭代.
    *   子图中顶点数量的最小值为 2,最大值为 图的顶点数量.
    *
    * strongly Connected Components
    *   构建连通的子图: 找到图中各个连通的部分,并以各部分中顶点ID最小值,标记该部分的所有顶点,
    *                若有与其他顶点都不连通的顶点,则用自己的ID值标记自己.
    *   该方法 会 考虑边的方向,所以,当子图中任意两点顶点都是连通的,则在子图中,至少有一个环.
    *   该方法中有一个Int 类型的参数 numIter,决定查找计算子图时的迭代次数:
    *       次数太少,会影响子图的发现,
    *       次数太多,影响不是很大,底层调用 Pregel进行迭代计算,当没有消息发送时也会停止迭代.
    *   子图中顶点数量的最小值为 2,最大值为 图的顶点数量.
    *
    */

}
