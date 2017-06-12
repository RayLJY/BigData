package spark.graphx.api

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.reflect.ClassTag

/**
  * Created by Ray on 2017/5/28.
  * PageRank algorithm example.
  */
object PageRankE {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession
      .builder
      .appName(s"${this.getClass.getSimpleName}")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    val vertexArray = Array(
      (1L, "A"),
      (2L, "B"),
      (3L, "C"),
      (4L, "D")
    )
    val edgeArray = Array(
      Edge(1L, 2L, 1),
      Edge(1L, 3L, 1),
      Edge(1L, 4L, 1),
      Edge(2L, 1L, 1),
      Edge(2L, 4L, 1),
      Edge(3L, 1L, 1),
      Edge(4L, 2L, 1),
      Edge(4L, 3L, 1)
    )

    val vertex: RDD[(Long, String)] = sc.parallelize(vertexArray)
    val edge: RDD[Edge[Int]] = sc.parallelize(edgeArray)

    // Tolerance 收敛值
    val tol=0.149
    // Load the edges as a graph
    val graph = Graph(vertex,edge)

    println("\ngraph : ")
    graph.triplets.foreach(println)

    // Run PageRank
    val pageRank = graph.pageRank(tol)
    println("\npageRank : ")
    pageRank.triplets.foreach(println)

    // Run pageRankCore
    val defPageRank = pageRankCore(graph,tol)
    println("\ndefPageRank : ")
    defPageRank.triplets.foreach(println)

    // Run pageRankCore with the source vertex,to create a Personalized Page Rank
    val defPageRankP = pageRankCore(graph,tol,srcId = Some(1))
    println("\ndefPageRankP : ")
    defPageRankP.triplets.foreach(println)

    spark.stop()
  }

  def pageRankCore[VD: ClassTag, ED: ClassTag]
  (graph: Graph[VD, ED], tol: Double, resetProb: Double = 0.15,
   srcId: Option[VertexId] = None): Graph[Double, Double]
  = {
    require(tol >= 0, s"Tolerance must be no less than 0, but got ${tol}")
    require(resetProb >= 0 && resetProb <= 1, s"Random reset probability must belong" +
      s" to [0, 1], but got ${resetProb}")

    val personalized = srcId.isDefined
    val src: VertexId = srcId.getOrElse(-1L)

    // Initialize the pagerankGraph with each edge attribute
    // having weight 1/outDegree and each vertex with attribute 1.0.
    val pagerankGraph: Graph[(Double, Double), Double] = graph
      // Associate the out degree with each vertex
      .outerJoinVertices(graph.outDegrees) {
      (vid, vdata, deg) => deg.getOrElse(0)
    }
      // Set the weight on the edges based on the out degree
      .mapTriplets(e => 1.0 / e.srcAttr)
      // Set the vertex attributes to (initialPR, delta = 0) // delta 变量增量
      .mapVertices { (id, attr) =>
      if (id == src) (resetProb, Double.NegativeInfinity) else (0.0, 0.0)
    }
      .cache()

    // Define the three functions needed to implement PageRank in the GraphX
    // version of Pregel
    def vertexProgram(id: VertexId, attr: (Double, Double), msgSum: Double): (Double, Double) = {
      val (oldPR, lastDelta) = attr
      val newPR = oldPR + (1.0 - resetProb) * msgSum
      // msgSum ：
      // 初始化时 ：msgSum 为 initialMessage
      // 迭代计算时 ：msgSum 为 messageCombiner 函数合并以后的值,即各个顶点贡献给该顶点的PageRank值的和.
      (newPR, newPR - oldPR)
    }

    def personalizedVertexProgram(id: VertexId, attr: (Double, Double),
                                  msgSum: Double): (Double, Double) = {
      val (oldPR, lastDelta) = attr
      var teleport = oldPR
      val delta = if (src == id) 1.0 else 0.0
      teleport = oldPR * delta

      val newPR = teleport + (1.0 - resetProb) * msgSum
      val newDelta = if (lastDelta == Double.NegativeInfinity) newPR else newPR - oldPR
      (newPR, newDelta)
    }

    def sendMessage(edge: EdgeTriplet[(Double, Double), Double]) = {
      if (edge.srcAttr._2 > tol) {
        Iterator((edge.dstId, edge.srcAttr._2 * edge.attr))
      } else {
        Iterator.empty
      }
    }

    def messageCombiner(a: Double, b: Double): Double = a + b

    // The initial message received by all vertices in PageRank
    val initialMessage = if (personalized) 0.0 else resetProb / (1.0 - resetProb)

    // Execute a dynamic version of Pregel.
    val vp = if (personalized) {
      (id: VertexId, attr: (Double, Double), msgSum: Double) =>
        personalizedVertexProgram(id, attr, msgSum)
    } else {
      (id: VertexId, attr: (Double, Double), msgSum: Double) =>
        vertexProgram(id, attr, msgSum)
    }

    Pregel(pagerankGraph, initialMessage, activeDirection = EdgeDirection.Out)(
      vp, sendMessage, messageCombiner)
      .mapVertices((vid, attr) => attr._1)
  }

  /**
    * PageRank 原理:
    *     PageRank 能够对网页的重要性做出客观评价,将从网页 A 指向网页 B 的链接解释为由网页 A 对网页 B 所投的一票.
    * 这样,PageRank 会根据网页 B 所收到的投票数量来评估该网页的重要性.此外,PageRank 还会评估每个投票网页的重要性,
    * 因为某些重要网页的投票被认为具有较高的价值,这样,它所链接的网页就能获得较高的价值.这就是 PageRank 的核心思想,
    * 实际算法要复杂很多.
    *
    *   假设：每一个网页的初始重要性是相同的,即它们初始的 PageRank 值是相等的.
    *       理论上已经证明了不论初始值如何选取,PageRank 算法都能保证迭代时的收敛性,即有真实值.
    *
    *  PageRank(A,n)= a + d(PageRank(B,n-1) / N(B) * R(A,B) + PageRank(C,n-1) / N(C) * R(A,C) + ...)
    *
    *  PageRank(A,n) 表示 第 n 次迭代计算时 A 页面的重要性.
    *  a 表示 PageRank 的初始值. 通常设置为 (1-d).
    *  d 比例系数.表示给 A 页面提供的 PageRank 值只能是自己页面 PageRank 值的 d 倍.d通常为 0.85.
    *  PageRank(B,n-1) 表示 第 n-1 次迭代计算时 B 页面的重要性.
    *  N(B) 表示 B 需要给多少个页面投票,即 B 顶点的出度值.
    *  R(A,B) 表示 A 与 B 之间的重要性,即 BA弧的属性值(权重).
    *
    * 根据每个页面的初始值(相等),算出各个网页的第一次 PageRank值(不等),
    * 然后再根据第一次的 PageRank 值,算出每个页面的第二次 PageRank 值.
    * ...
    * 直到,一个页面相邻的两次 PageRank值 的差值 小于某个固定值(可设置).
    * 此时的 PageRank值,即为 A 页面的 PageRank值.
    *
    * Spark GraphX 中的 PageRank 实现:
    *
    *   基于 Pregel Object 实现.
    *
    *   RageRank 的初始值为 0.15
    *   比例系数 d 为 (1-初始值) = 0.85
    *   收敛值需要自己设置
    *
    *   实现过程：
    *     0. 初始化每一个顶点的属性，即设置为 (0.0, 0.0),表示 (initialPR, delta).
    *     1. pregel 操作:
    *       0). 使用 vertexProgram 函数对每一个顶点设置 PageRank初始值,即(0.15,0.15)
    *       1). 迭代计算:
    *             计算出一个新的 PageRank值,和旧的比较,如果满足收敛条件,就停止.
    *             同时,每次更新有变化的顶点属性.
    *
    * PageRank 函数提供了两种计算方式,一种是全局的 Normal PageRank值计算,另一种是计算全局的 Personalized PageRank值.
    *
    * Normal PageRank:
    *     假设一个用户会以 p 的概率沿着当前页面包含的超链接浏览其他网页，同时以 1−p 的概率随机浏览其它一个网页.
    * Personalized PageRank:
    *     在 Normal PageRank 的基础上,会对某一个页面设置额外的权重(初始值),用来模拟用户对某些网页的偏爱,产生的行为偏向.
    *
    */

}
