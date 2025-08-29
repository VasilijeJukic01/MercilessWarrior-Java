package analytics.jobs

import analytics.SparkSessionTestWrapper
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.junit.jupiter.api.Tag
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

@Tag("unit")
class BatchAnalyticsJobTest extends AnyWordSpec with Matchers with SparkSessionTestWrapper {

  import spark.implicits._

  "BatchAnalyticsJob" should {

    "calculate top purchased items correctly on a happy path" in {
      // Arrange
      val transactionData = Seq(
        ("user1", "BUY", "item_001", 2, 1000, 2000),
        ("user2", "BUY", "item_001", 1, 1100, 1100),
        ("user1", "BUY", "item_002", 10, 10, 100),
        ("user3", "BUY", "item_002", 5, 10, 50),
        ("user4", "BUY", "item_003", 1, 2000, 2000),
        ("user1", "SELL", "item_002", 3, 5, 15)
      )
      val transactionsDf = transactionData.toDF("userId", "transactionType", "itemId", "quantity", "unitPrice", "totalPrice")

      val batchJob = new BatchAnalyticsJob(spark)

      // Act
      val topItemsDf = batchJob.calculateTopPurchasedItems(transactionsDf)

      // Assert
      val expectedData = Seq(
        Row("Health Potion", "Common", 15L, 2L, 150L, "10.00"),
        Row("Sword of Valor", "Epic", 3L, 2L, 3100L, "1,050.00"),
        Row("Dragonscale Shield", "Legendary", 1L, 1L, 2000L, "2,000.00")
      )

      val expectedSchema = StructType(Seq(
        StructField("name", StringType, nullable = true),
        StructField("rarity", StringType, nullable = true),
        StructField("total_purchased", LongType, nullable = true),
        StructField("unique_buyers", LongType, nullable = false),
        StructField("total_revenue", LongType, nullable = true),
        StructField("avg_price", StringType, nullable = true)
      ))

      val expectedDf = spark.createDataFrame(spark.sparkContext.parallelize(expectedData), expectedSchema)
      assertDataFrameEquals(topItemsDf, expectedDf)
    }

    "return an empty DataFrame when the input transactions are empty" in {
      // Arrange
      val emptyTransactionsDf = spark.createDataFrame(
        spark.sparkContext.emptyRDD[Row],
        StructType(Seq(
          StructField("userId", StringType),
          StructField("transactionType", StringType),
          StructField("itemId", StringType),
          StructField("quantity", LongType),
          StructField("unitPrice", LongType),
          StructField("totalPrice", LongType)
        ))
      )
      val batchJob = new BatchAnalyticsJob(spark)

      // Act
      val topItemsDf = batchJob.calculateTopPurchasedItems(emptyTransactionsDf)

      // Assert
      topItemsDf.count() should be(0)
    }

    "return an empty DataFrame when there are no 'BUY' transactions" in {
      // Arrange
      val transactionData = Seq(
        ("user1", "SELL", "item_001", 2, 800, 1600),
        ("user2", "SELL", "item_002", 5, 5, 25)
      )
      val transactionsDf = transactionData.toDF("userId", "transactionType", "itemId", "quantity", "unitPrice", "totalPrice")
      val batchJob = new BatchAnalyticsJob(spark)

      // Act
      val topItemsDf = batchJob.calculateTopPurchasedItems(transactionsDf)

      // Assert
      topItemsDf.count() should be(0)
    }

    "ignore transactions for items not present in the item master data" in {
      // Arrange
      val transactionData = Seq(
        ("user1", "BUY", "item_001", 5, 1000, 5000),
        ("user2", "BUY", "item_999_unknown", 1, 9999, 9999)
      )
      val transactionsDf = transactionData.toDF("userId", "transactionType", "itemId", "quantity", "unitPrice", "totalPrice")
      val batchJob = new BatchAnalyticsJob(spark)

      // Act
      val topItemsDf = batchJob.calculateTopPurchasedItems(transactionsDf)

      // Assert
      topItemsDf.count() should be(1)
      val resultRow = topItemsDf.collect().head
      resultRow.getAs[String]("name") should be("Sword of Valor")
      resultRow.getAs[Long]("total_purchased") should be(5)
    }
  }

  /**
   * Helper function to compare two DataFrames by content, ignoring row order.
   */
  def assertDataFrameEquals(actualDf: DataFrame, expectedDf: DataFrame): Unit = {
    actualDf.schema shouldEqual expectedDf.schema
    val actualData = actualDf.collect().map(_.toString()).sorted
    val expectedData = expectedDf.collect().map(_.toString()).sorted
    actualData should contain theSameElementsAs expectedData
  }
}