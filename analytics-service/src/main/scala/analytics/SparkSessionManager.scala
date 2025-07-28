package analytics

import org.apache.spark.sql.SparkSession

object SparkSessionManager {

  def getSparkSession(appName: String): SparkSession = {
    SparkSession.builder()
      .appName(appName)
      .master("local[*]")
      .config("spark.sql.streaming.checkpointLocation", "data/checkpoints")
      .getOrCreate()
  }

}
