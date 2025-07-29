package analytics

import analytics.jobs.BatchAnalyticsJob

object BatchAnalytics {

  def main(args: Array[String]): Unit = {
    val spark = SparkSessionManager.getSparkSession("MercilessWarriorBatchAnalytics")
    val job = new BatchAnalyticsJob(spark)
    job.run()
    spark.stop()
  }

}