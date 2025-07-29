package analytics

import analytics.jobs.StreamAnalyticsJob

object StreamAnalytics {

  def main(args: Array[String]): Unit = {
    val spark = SparkSessionManager.getSparkSession("MercilessWarriorAnalytics")
    val job = new StreamAnalyticsJob(spark)
    job.run()
  }

}