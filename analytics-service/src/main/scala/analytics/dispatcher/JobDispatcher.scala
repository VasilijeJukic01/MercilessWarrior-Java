package analytics.dispatcher

import analytics.SparkSessionManager
import analytics.jobs.{BatchAnalyticsJob, StreamAnalyticsJob}

object JobDispatcher {

  def main(args: Array[String]): Unit = {
    val jobType = if (args.length > 0) args(0).toLowerCase else "stream"

    val spark = SparkSessionManager.getSparkSession(s"MercilessWarriorAnalytics-$jobType")

    jobType match {
      case "stream" =>
        println("Starting Streaming Analytics Job...")
        val job = new StreamAnalyticsJob(spark)
        job.run()

      case "batch" =>
        println("Starting Batch Analytics Job...")
        val job = new BatchAnalyticsJob(spark)
        job.run()
        println("Batch Analytics Job finished. Shutting down Spark session.")
        spark.stop()

      case _ =>
        println(s"Unknown job type: '$jobType'. Please specify 'stream' or 'batch'.")
        spark.stop()
    }
  }

}