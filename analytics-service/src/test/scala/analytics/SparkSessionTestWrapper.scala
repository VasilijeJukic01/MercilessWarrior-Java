package analytics

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, Suite}

/**
 * A trait to provide a SparkSession for testing purposes.
 * It uses a lazy val to ensure the SparkSession is stable for imports and is only created once when first needed.
 * It is stopped after all tests.
 */
trait SparkSessionTestWrapper extends BeforeAndAfterAll { self: Suite =>

  lazy val spark: SparkSession = {
    SparkSession.builder()
      .appName("Analytics Test Suite")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "1")
      .config("spark.driver.host", "127.0.0.1")
      .config("spark.ui.enabled", "false")
      .getOrCreate()
  }

  override def afterAll(): Unit = {
    if (spark != null) spark.stop()
    super.afterAll()
  }
}