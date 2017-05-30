package Ray.spark.graphX

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/5/27.
  * SSSP = single source shortest path.
  */
object SSSP {

  def main(args: Array[String]): Unit = {

    // off log
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val spark = SparkSession
      .builder
      .appName(s"${this.getClass.getSimpleName}")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    // A graph with edge attributes containing distances
    val graph: Graph[Long, Double] =
    GraphGenerators.logNormalGraph(sc, numVertices = 5).mapEdges(e => e.attr.toDouble)

    val sourceId: VertexId = 3 // The ultimate source

    // Initialize the graph such that all vertices except the root have distance infinity.
    val initialGraph = graph.mapVertices((id, _) =>
      if (id == sourceId) 0.0 else Double.PositiveInfinity)

    println("initialGraph : ")
    initialGraph.triplets.foreach(println)

    val sssp = initialGraph.pregel(
      // initialMsg
      Double.PositiveInfinity
    )(
      // Vertex Program
      (id, dist, newDist) => math.min(dist, newDist),

      // Send Message
      triplet => {
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },

      // Merge Message
      (a, b) => math.min(a, b)
    )

    println("\nsssp : ")
    sssp.vertices.foreach(println)

    spark.stop()
  }

  /**
    * 首先将所有除了起始顶点以外,其它顶点的属性值设置为无穷大,起始顶点的属性值设置为 0.
    * 然后进行 pregel 操作:
    * Super step 0 : 对所有顶点用 initialMsg 进行初始化,实际上这次初始化是针对每一个顶点设置(不是发送)了一个初始消息,
    *                形成一个新的数据结构(id, dist, initialMsg).
    *                使用  vprog: (id, dist, newDist) => math.min(dist, newDist) 函数进行初始化设置,
    *                id 是顶点ID.
    *                dist 是顶点属性值,即 Double.PositiveInfinity(或 0).
    *                newDist 是 pregel 函数的第一个参数,即 initialMsg,也就是 Double.PositiveInfinity.
    *
    * Super step 1 : 对于每个 triplet（可以理解为是图的一条边):
    *                triplet.srcAttr 是起始点的属性值,即 Double.PositiveInfinity(或 0).
    *                triplet.attr 是边的属性值,之前从未设置过,默认为 1.
    *                triplet.dstAttr 是终点的属性值,即 Double.PositiveInfinity(或 0).
    *
    *                triplet.srcAttr + triplet.attr < triplet.dstAttr
    *                只有在 triplet 表示的边的起始顶点 与 设置的起始顶点相同时 才为 true.
    *
    *                如果成立,就发送一条消息 Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
    *                不成立,就发送一条消息 Iterator.empty.
    *
    *                对所有的边进行如上操作,完成以后,收集所有的消息,进行 mergeMsg:(a, b) => math.min(a, b)
    *                基于 triplet.dstId 合并,a 和 b 都代表 triplet.srcAttr + triplet.attr 的值.
    *                这里返回值不是叠加,而是取最小值.
    *
    * Super step 2 : 消息合并完成以后,基于消息集合中的 triplet.dstId 值，构建一个新的顶点集合 newVertex,
    *                其中每个顶点的属性值即为 mergeMsg 函数的返回值(也是之后进行 join 时使用的 newDist).
    *                然后基于 vprog：(id, dist, newDist) => math.min(dist, newDist) 函数,
    *                将原始图数据 和 newVertex 进行 joinVertices
    *                即,将 newVertex 中新的属性值 替换掉 原始图数据中的属性值.
    *
    *
    * Super step 3 : 使用 更新的 图数据 进行 Super step 1 和 Super step 2 操作,直到产生的消息为 0. 返回 最新的 图.
    *                此时每个顶点的属性值,即为 指定顶点 到 该顶点 的最短路径(代价).
    */

}
