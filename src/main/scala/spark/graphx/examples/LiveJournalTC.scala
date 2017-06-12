package spark.graphx.examples

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{GraphLoader, GraphXUtils}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Ray on 2017/6/1.
  * Uses GraphX to run TriangleCount on a LiveJournal social network graph.
  * Download the dataset from http://snap.stanford.edu/data/soc-LiveJournal1.txt.gz.
  *   Directed LiveJournal friednship social network
  *   Nodes: 4847571
  *   Edges: 68993773
  * For more information, browse http://snap.stanford.edu/data/soc-LiveJournal1.html.
  */
object LiveJournalTC {

  val dataPath = "data/spark/graphx/part3.txt"

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val conf = new SparkConf()
      .setAppName("LiveJournal triangle count")
      .setMaster("local")

    // 更换 spark 默认使用的 JavaSerializer 序列化器,使用 Kryo 序列化器
    GraphXUtils.registerKryoClasses(conf)

    val sc = new SparkContext(conf)

    val graph = GraphLoader.edgeListFile(sc,dataPath,
      canonicalOrientation = true,
      edgeStorageLevel = StorageLevel.MEMORY_ONLY,
      vertexStorageLevel = StorageLevel.MEMORY_ONLY
    ).cache()

    println("GRAPHX: Number of vertices " + graph.vertices.count)
    println("GRAPHX: Number of edges " + graph.edges.count)

    val triangles = graph.triangleCount()

    val tc = triangles.vertices.map {
      case (vid, data) => data.toLong
    }.reduce(_ + _) / 3

    println("Triangles: " + tc)

    sc.stop()

  }

}
