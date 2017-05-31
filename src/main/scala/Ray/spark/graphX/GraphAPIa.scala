package Ray.spark.graphX

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/5/26.
  * Transform vertex and edge attributes of graph.
  */
object GraphAPIa {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession.builder()
      .appName("GraphX API Test - Transform vertex and edge attributes")
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
    // Transform vertex and edge attributes
    println("Transform vertex and edge attributes")

    // def mapVertices[VD2](map: (VertexId, VD) => VD2): Graph[VD2, ED]
    // def mapEdges[ED2](map: Edge[ED] => ED2): Graph[VD, ED2]
    // def mapTriplets[ED2](map: EdgeTriplet[VD, ED] => ED2): Graph[VD, ED2]

    // 每个操作都产生一个新的图,这个新的图包含通过用户自定义的map函数操作修改后的顶点或边的属性.
    // 注意,每种情况下图结构都不受影响.这些操作的一个重要特征是它允许所得图形重用原有图形的结构索引(indices).

    // 下面的两类代码在逻辑上是等价的,但是第一个不保存结构索引,所以不会从GraphX系统优化中受益.
    // 1
    val newVertices = graph.vertices.map { case (id, attr) => (id, mapUdf(id, attr)) }
    val newGraph1 = Graph(newVertices, graph.edges)
    // 2
    val newGraph2 = graph.mapVertices((id, attr) => mapUdf(id, attr))

    println("\nnewGraph1 : ")
    newGraph1.vertices.foreach(println)
    println("\nnewGraph2 : ")
    newGraph2.vertices.foreach(println)

    val newGraph3 = graph.mapEdges(e => e.attr * 100)
    println("\nnewGraph3 : ")
    newGraph3.triplets.foreach(println)

    // Given a graph where the vertex property is the out degree
    val inputGraph: Graph[Int, Int] =
      graph.outerJoinVertices(graph.outDegrees)((vid, _, degOpt) => degOpt.getOrElse(0))

    // Construct a graph where each edge contains the weight (1.0 / value of property of source vertex)
    // and each attr of vertex set 1.0
    val outputGraph: Graph[Double, Double] =
    inputGraph.mapTriplets(triplet => 1.0 / triplet.srcAttr).mapVertices((id, _) => 1.0)

    println("\ninputGraph : ")
    inputGraph.triplets.foreach(println)
    println("\noutputGraph : ")
    outputGraph.triplets.foreach(println)

    spark.close()
  }

  // do nothing
  def mapUdf(id: VertexId, attr: (String, Int)) = (id,attr)
}
