package spark.graphx.api

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/5/26.
  * Join RDDs with the graph.
  */
object GraphAPIc {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession.builder()
      .appName("GraphX API Test - Join RDDs with the graph")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    // vertex of graph
    val vertexArray = Array(
      (1L, 0.1),
      (2L, 0.2),
      (3L, 0.3),
      (4L, 0.4),
      (5L, 0.5),
      (6L, 0.6)
    )

    // edge of graph
    val edgeArray = Array(
      Edge(3L, 7L, 1.0),
      Edge(5L, 3L, 2.0),
      Edge(2L, 5L, 3.0),
      Edge(5L, 7L, 4.0),
      Edge(4L, 0L, 5.0),
      Edge(5L, 0L, 6.0)
    )

    val vertexRDD: RDD[(VertexId, Double)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[(Edge[Double])] = sc.parallelize(edgeArray)

    // Build the initial Graph
    val graph: Graph[Double, Double] = Graph(vertexRDD, edgeRDD)

    println("graph : ")
    graph.triplets.foreach(println)

    // Join RDDs with the graph

    // join 操作是针对顶点属性值的操作.将额外的属性合并到已有的图中,或者从一个图中取出顶点属性值加入到另外一个图中.

    // def joinVertices[U](table: RDD[(VertexId, U)])(map: (VertexId, VD, U) => VD) : Graph[VD, ED]
    // def outerJoinVertices[U,VD2](table:RDD[(VertexId, U)])(map:(VertexId,VD,Option[U]) => VD2):Graph[VD2, ED]

    // joinVertices 操作,将输入的 VertexRDD 和顶点相结合,返回一个新的带有顶点特征的图.
    // 新的顶点特征是通过在连接顶点后,使用自定义的函数的返回值.
    // 在 VertexRDD 中没有被匹配的顶点,则保留其原始值.
    // 对于给定的顶点,如果 VertexRDD 中有超过1个的匹配值,则仅仅使用其中的一个.
    // 建议保证输入 VertexRDD 中, VertexId 值的唯一性.
    // 下面的方法也会预留索引返回的值用以加快后续的join操作.

    val nonUnique: RDD[(VertexId, Double)] = sc.parallelize(Array((0L, 1.0), (0L, 1.0), (2L, 2.0)))

    // 基于顶点ID值进行合并
    val uniqueCosts: VertexRDD[Double] =
      graph.vertices.aggregateUsingIndex(nonUnique, (a, b) => a + b)

    val joinedGraph = graph.joinVertices(uniqueCosts)(
      (id, oldCost, extraCost) => oldCost + extraCost)

    println("\nnonUnique : ")
    nonUnique.foreach(println)

    println("\nuniqueCosts : ")
    uniqueCosts.foreach(println)

    println("\njoinedGraph : ")
    joinedGraph.triplets.foreach(println)


    // outerJoinVertices 操作
    // 因为并不是所有顶点在 VertexRDD 中拥有匹配的值,自定义函数需要一个option类型.

    // 取ID大于4的顶点,并以该顶点的出度值为属性值
    val outDegrees: VertexRDD[Int] = graph.outDegrees.filter(_._1 > 4)
    val degreeGraph = graph.outerJoinVertices(outDegrees) { (id, oldAttr, outDegreesOpt) =>
      outDegreesOpt match {
        case Some(outDegreesOpt) => outDegreesOpt + oldAttr
        case None => 0.0 // No outDegree means zero outDegree
      }
    }

    println("\noutDegrees : ")
    outDegrees.foreach(println)

    println("\ndegreeGraph : ")
    degreeGraph.triplets.foreach(println)

    spark.close()

  }

}
