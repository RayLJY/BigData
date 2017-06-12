package spark.graphx.examples

import org.apache.spark.sql.SparkSession

/**
  * Created by Ray on 2017/6/1.
  * Randomly split data.
  */
object Utils {

  val dataPath = "data/spark/graphx/soc-LiveJournal.txt"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Split data")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext

    val file = sc.textFile(dataPath)

     file
       .randomSplit(Array(0.1,0.9))(0)
       .coalesce(1)
       .saveAsTextFile("res/data")

    // close spark session
    spark.stop()

  }

}
