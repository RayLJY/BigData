package spark.graphx.api

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/5/31.
  */
object TriangleCountingE {

  def main(args: Array[String]): Unit = {
    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession.builder()
      .appName("connected components")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    /**
      * graph :
      *
      *   1---2---3
      *   |  /   /|
      *   | /   / |
      *   4    5--6
      */


    // vertex of graph
    val vertexArray = Array(
      (1L, 0.0),
      (2L, 0.0),
      (3L, 0.0),
      (4L, 0.0),
      (5L, 0.0),
      (6L, 0.0)
    )

    // edge of graph
    val edgeArray = Array(
      Edge(1L, 2L, 0),
      Edge(2L, 3L, 0),
      Edge(2L, 4L, 0),
      Edge(3L, 5L, 0),
      Edge(6L, 3L, 0),
      Edge(4L, 1L, 0),
      Edge(5L, 6L, 0),
      Edge(1L, 1L, 0)
    )

    val vertexRDD: RDD[(VertexId, Double)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[(Edge[Int])] = sc.parallelize(edgeArray)

    // Build the initial Graph
    val graph: Graph[Double, Int] = Graph(vertexRDD, edgeRDD)

    println("\ngraph : ")
    graph.triplets.foreach(println)


    val canonicalGraph = graph.mapEdges(e => true)
      .removeSelfEdges()            // 去除自环
      .convertToCanonicalEdges()    // 将每一条边/弧 转换成 起始顶点ID值 小于 终止顶点的ID值
    println("\ncanonicalGraph : ")
    canonicalGraph.triplets.foreach(println)

    val triCount = graph.triangleCount()

    println("\ntriCount : ")
    triCount.vertices.foreach(println)

    // stop spark session
    spark.stop()

  }

  /**
    * TriangleCount 主要用途之一是用于社区发现,Triangle就是指一个"三角形",这个三角形是一个三结点的连通子图,其中顶点两两相连.
    * 将全部数据构成一个图,将图拆分成一个个连通的子图,在每一个子图中统计这样的"三角形",如果在某一个子图中有足够多的"三角形",
    * 这个子图就可以构成一个社区.例如,在微博上你关注的人也互相关注,大家的关注关系中就会有很多"三角形",这说明大家的联系都比较紧密,
    * 就可以形成一个稳定性很强的社区;同样,在一个已有的社区中,如果这样的"三角形"很少,说明这个社区中的关系比较松散,彼此的联系比较少.
    *
    * TriangleCount 的算法实现如下:
    *   0. 找到每个顶点的邻接顶点
    *   1. 对每条边计算交集,并找出交集中id大于前两个顶点id的顶点
    *   2. 对每个顶点统计 Triangle 总数,注意只统计符合计算方向的 TriangleCount.
    *
    *   注意:计算方向(起始顶点id < 中间顶点id < 目的顶点id).
    *   假设顶点A 和顶点B 是邻居,顶点A 的邻接顶点集合是{B,C,D,E},顶点B 的邻接顶点集合是{A,C,E,F,G},而它们的交集是{C,E}.
    *   交集中的顶点是顶点A 和结点B 的共同邻结点,所以有{A,B,C}和{A,B,E}两个三角形.
    *
    */

}
