package analytics

import analytics.jobs.GameAnalyticsStreamJob

object Main {

  def main(args: Array[String]): Unit = {
    val spark = SparkSessionManager.getSparkSession("MercilessWarriorAnalytics")
    val job = new GameAnalyticsStreamJob(spark)
    job.run()
  }

}