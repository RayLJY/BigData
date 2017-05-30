package Ray.spark.graphX

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/5/26.
  * Modify the graph structure.
  */
object GraphAPIb {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession.builder()
      .appName("GraphX API Test - Modify the graph structure")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    // vertex of graph
    val vertexArray = Array(
      (3L, ("rxin", "student")),
      (7L, ("jgonzal", "postdoc")),
      (5L, ("franklin", "prof")),
      (4L, ("peter", "student")),
      (2L, ("istoica", "prof"))
    )

    // edge of graph
    val edgeArray = Array(
      Edge(3L, 7L, "collab"),
      Edge(5L, 3L, "advisor"),
      Edge(2L, 5L, "colleague"),
      Edge(5L, 7L, "pi"),
      Edge(4L, 0L, "student"),
      Edge(5L, 0L, "colleague")
    )

    val users: RDD[(VertexId, (String, String))] = sc.parallelize(vertexArray)
    val relationships: RDD[(Edge[String])] = sc.parallelize(edgeArray)

    // Define a default user in case there are relationship with missing user
    val defaultUser = ("John Doe", "Missing")

    // Build the initial Graph
    val graph: Graph[(String, String), String] = Graph(users, relationships, defaultUser)

    println("graph : ")
    graph.triplets.foreach(println)

    // Modify the graph structure

    // def reverse: Graph[VD, ED]
    // def subgraph(epred: EdgeTriplet[VD,ED] => Boolean, vpred: (VertexId, VD) => Boolean): Graph[VD, ED]
    // def mask[VD2, ED2](other: Graph[VD2, ED2]): Graph[VD, ED]
    // def groupEdges(merge: (ED, ED) => ED): Graph[VD,ED]

    // reverse 操作返回一个新的图，这个图的边的方向都是反转的。
    // 因为反转操作没有修改顶点或者边的属性或者改变边的数量，所以我们可以在不移动或者复制数据的情况下有效地实现它.

    val reverse = graph.reverse
    println("\nreverse : ")
    reverse.triplets.foreach(println)

    // subgraph 操作,利用顶点和边的谓词(predicates),返回的图仅仅包含满足顶点谓词的顶点、满足边谓词的边
    // 以及满足顶点谓词的连接顶点(connect vertices).
    // 如果没有提供顶点或者边的谓词,subgraph 操作默认为true.

    // Notice that there is a user 0 (for which we have no information) connected
    // to users 4 (peter) and 5 (franklin).
    // Remove missing vertices as well as the edges to connected to them
    val validGraph = graph.subgraph(vpred = (id, attr) => attr._2 != "Missing")
    // The valid subgraph will disconnect users 4 and 5 by removing user 0
    println("\nvalidGraph : ")
    validGraph.triplets.foreach(println)


    // mask 操作,以另一个图(参数)中包含的顶点和边，构造该图的一个子图。
    // 这个操作可以和subgraph操作相结合，基于另外一个相关图的特征去约束一个图。

    // connectedComponents 计算每一个顶点的连通性，并返回一个包含所有连通顶点的图，包其中顶点的属值是设置为：连通顶点中ID值最小的值
    val ccGraph = graph.connectedComponents() // No longer contains missing field
    // Restrict the answer to the valid subgraph
    val validCCGraph = ccGraph.mask(validGraph)

    println("\nccGraph : ")
    ccGraph.triplets.foreach(println)

    println("\nvalidCCGraph : ")
    validCCGraph.triplets.foreach(println)

    // groupEdges 操作合并多重图中的并行边(如顶点对之间重复的边)。
    // 在大量的应用程序中，并行的边可以合并（它们的权重合并）为一条边从而降低图的大小。

    // no example

    spark.close()
  }
}
