package Ray.spark.graphX.examples

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.lib.ConnectedComponents
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx.{GraphLoader, GraphXUtils}
import org.apache.spark.storage.StorageLevel

/**
  * Created by Ray on 2017/6/1.
  * Uses GraphX to run connected components on a LiveJournal social network graph.
  * Download the dataset from http://snap.stanford.edu/data/soc-LiveJournal1.txt.gz.
  *   Directed LiveJournal friednship social network
  *   Nodes: 4847571
  *   Edges: 68993773
  * For more information, browse http://snap.stanford.edu/data/soc-LiveJournal1.html.
  */
object LiveJournalCC {

  val dataPath = "data/spark/graphx/part3.txt"
  val resultPath = "res/cc"

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)

    val conf = new SparkConf()
      .setAppName("LiveJournal connected components")
      .setMaster("local")

    // 更换 spark 默认使用的 JavaSerializer 序列化器,使用 Kryo 序列化器
    GraphXUtils.registerKryoClasses(conf)

    val sc = new SparkContext(conf)

    val graph = GraphLoader.edgeListFile(sc,dataPath,
      edgeStorageLevel = StorageLevel.MEMORY_ONLY,
      vertexStorageLevel = StorageLevel.MEMORY_ONLY
    ).cache()

    println("GRAPHX: Number of vertices " + graph.vertices.count)
    println("GRAPHX: Number of edges " + graph.edges.count)

    val cc = graph.connectedComponents()
    val ccv = cc.vertices.map { case (vid, data) => data }.distinct()

    ccv.saveAsTextFile(resultPath)

    sc.stop()

  }

}
