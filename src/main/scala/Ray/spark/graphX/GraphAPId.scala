package Ray.spark.graphX

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/5/26.
  * Aggregate information about adjacent triplets.
  */
object GraphAPId {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession.builder()
      .appName("GraphX API Test - Aggregate information about adjacent triplets")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    // Aggregate information about adjacent triplets

    // 相邻聚合（Neighborhood Aggregation）
    // 图分析任务的一个关键步骤是汇总每个顶点附近的信息.
    // 许多迭代图算法(PageRank、最短路径和连通体)需要多次聚合相邻顶点的属性.

    // GraphX 中的核心聚合操作是 aggregateMessages.

    // def aggregateMessages[Msg: ClassTag](
    //                                       sendMsg: EdgeContext[VD, ED, Msg] => Unit,
    //                                       mergeMsg: (Msg, Msg) => Msg,
    //                                       tripletFields: TripletFields = TripletFields.All
    //                                      ):VertexRDD[Msg]

    // aggregateMessages 操作是基于每一条 边 的,然后将用户定义的 sendMsg 函数应用到图的每一条 边 上,
    // 通过 sendMsg 的属性的函数 sendToSrc 和 sendToDst 可以分别向边的起点顶点和终点顶点发送信息(Msg类型),
    // 当所有的 边 进行完成 sendMsg 函数的操作后,然后应用 mergeMsg 函数,在每一个顶点上聚合该顶点收到的信息(Msg).
    // 最终返回一个包含聚合消息的顶点集 VertexRDD[Msg],没有接收到消息的顶点不包含在返回的 VertexRDD[Msg]中.

    // 柯里化(Currying)是把接受多个参数的函数变换成接受一个单一参数(最初函数的第一个参数)的函数,
    // 并且返回值接受余下的参数且返回结果的新函数的技术.
    // sendMsg 第一个参数,mergeMag 第二个参数.

    // 可选的 tripletFields 参数,它指出在 sendMsg 过程中,哪些数据被访问(如起点、终点、边(本身)以及它们的属性值).
    // tripletFields 参数用来设置 EdgeContext 的一部分参与运算,使 GraphX 选择一个优化的连接策略.

    // Import random graph generation library
    import org.apache.spark.graphx.util.GraphGenerators

    val graph: Graph[Double, Int] =
      GraphGenerators.logNormalGraph(sc, numVertices = 10)  // 生成一个有10个顶点的图,每个顶点的出度符合正态分布.
        .mapVertices((id, _) => id * 1.0 )  // 设置每个顶点的属性值为:该顶点的ID值 * 1.0

    println("graph : ")
    graph.triplets.foreach(println)

    // compute the in degree of each vertex
    val inDeg: RDD[(VertexId, Int)] =
       graph.aggregateMessages[Int](ctx => ctx.sendToDst(1), _ + _)

    println("\ninDeg : ")
    inDeg.foreach(println)

    // compute the out degree of each vertex
    val outDeg: RDD[(VertexId, Int)] =
       graph.aggregateMessages[Int](ctx => ctx.sendToSrc(1), _ + _)

    println("\noutDeg : ")
    outDeg.foreach(println)

    // suppose :
    //   vertex represent a person
    //   value of vertex property is age of this person
    //   edge represent follow relation

    // Compute the number of older followers and the number of follower and their total age
    val olderFollowers: VertexRDD[(Int, Double)] = graph.aggregateMessages[(Int, Double)](
      triplet => {
        if (triplet.srcAttr > triplet.dstAttr) { // older follower
          // Send message to destination vertex containing counter and age
          triplet.sendToDst(1, triplet.srcAttr)
        }
      },
      // total follower and their total age
      (a, b) => (a._1 + b._1, a._2 + b._2)
    )

    println("\nolderFollowers :　")
    olderFollowers.foreach(println)

    // Divide total age by number of older followers to get average age of older followers
    val avgAgeOfOlderFollowers: VertexRDD[Double] =
      olderFollowers.mapValues((id, value) => value match {
        case (count, totalAge) => totalAge / count
      })

    println("\navgAgeOfOlderFollowers :　")
    avgAgeOfOlderFollowers.foreach(println)

    // 当消息（以及消息的总数）是常量大小(列表和连接替换为浮点数和添加)时,aggregateMessages 操作的效果最好.

    // 计算度信息
    // 最一般的聚合任务就是计算顶点的度,即每个顶点相邻边的数量.在有向图中,经常需要知道顶点的入度、出度以及度.
    // GraphOps 类包含一个操作集合用来计算每个顶点的度.例如,下面的例子计算最大的入度、出度和度.

    // Define a reduce operation to compute the highest degree vertex
    def max(a: (VertexId, Int), b: (VertexId, Int)): (VertexId, Int) = {
      if (a._2 > b._2) a else b
    }

    // Compute the max degrees
    val maxInDegree: (VertexId, Int) = graph.inDegrees.reduce(max)
    val maxOutDegree: (VertexId, Int) = graph.outDegrees.reduce(max)
    val maxDegrees: (VertexId, Int) = graph.degrees.reduce(max)

    println("\nmaxInDegree : " + maxInDegree)
    println("\nmaxOutDegree : " + maxOutDegree)
    println("\nmaxDegrees : " + maxDegrees)

    // Collecting Neighbors
    // 在某些情况下,通过收集每个顶点相邻的顶点及它们的属性来表达计算可能更容易.
    //   def collectNeighborIds(edgeDirection: EdgeDirection): VertexRDD[Array[VertexId]]
    //   def collectNeighbors(edgeDirection: EdgeDirection): VertexRDD[ Array[(VertexId, VD)]]
    // 这些操作是非常昂贵的,因为它们需要重复的信息和大量的通信.如果可能,尽量用 aggregateMessages 操作直接表达相同的计算.

    spark.close()
  }

}
