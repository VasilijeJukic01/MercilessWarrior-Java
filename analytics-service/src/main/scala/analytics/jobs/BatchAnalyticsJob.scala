package analytics.jobs

import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{BooleanType, DoubleType, LongType, MapType, StringType, StructField, StructType}

import scala.io.Source

/**
 * A batch job to read the shop transaction data from the data lake, perform aggregations, and generate reports.
 */
class BatchAnalyticsJob(spark: SparkSession) extends Logging {

  private val itemSchema = StructType(Seq(
    StructField("description", StringType, nullable = true),
    StructField("imagePath", StringType, nullable = true),
    StructField("name", StringType, nullable = true),
    StructField("rarity", StringType, nullable = true),
    StructField("sellValue", LongType, nullable = true),
    StructField("stackable", BooleanType, nullable = true),
    StructField("equip", StructType(Seq(
      StructField("bonuses", MapType(StringType, DoubleType, valueContainsNull = true), nullable = true),
      StructField("canEquip", BooleanType, nullable = true),
      StructField("slot", StringType, nullable = true)
    )), nullable = true),
  ))

  def run(): Unit = {
    import spark.implicits._
    val transactionsDf = spark.read
      .option("mergeSchema", "true")
      .parquet("data/lake/shop_transactions")

    logInfo(s"Loaded ${transactionsDf.count()} total transactions.")

    val itemMasterDf = loadItemMasterData(spark)
    logInfo("Calculating top 10 most purchased items with enriched data...")

    val topItems = transactionsDf
      .filter($"transactionType" === "BUY")
      .join(itemMasterDf, "itemId")
      .groupBy($"itemId", $"name", $"rarity")
      .agg(
        sum("quantity").as("total_purchased"),
        countDistinct("userId").as("unique_buyers"),
        sum("totalPrice").as("total_revenue"),
        avg("unitPrice").as("avg_price")
      )
      .orderBy(desc("total_purchased"))
      .limit(10)
      .select(
        $"name",
        $"rarity",
        $"total_purchased",
        $"unique_buyers",
        $"total_revenue",
        format_number($"avg_price", 2).as("avg_price")
      )

    topItems.show(truncate = false)

    logInfo("Batch Analytics Job finished.")
  }

  private def loadItemMasterData(spark: SparkSession): DataFrame = {
    import spark.implicits._

    val resourceStream = classOf[BatchAnalyticsJob].getResourceAsStream("/data/items.json")
    val jsonString = Source.fromInputStream(resourceStream).mkString
    resourceStream.close()

    val itemMasterDataset = spark.createDataset(Seq(jsonString))
    val wideDfTemp = spark.read.option("multiline", "true").json(itemMasterDataset)
    val itemIds = wideDfTemp.columns

    val fullSchema = StructType(itemIds.map(id => StructField(id, itemSchema, nullable = true)))

    val wideDf = spark.read
      .option("multiline", "true")
      .schema(fullSchema)
      .json(itemMasterDataset)

    val stackExpression = s"stack(${itemIds.length}, ${itemIds.map(c => s"'$c', `$c`").mkString(", ")}) as (itemId, itemData)"

    wideDf.selectExpr(stackExpression)
      .select($"itemId", $"itemData.*")
  }
}