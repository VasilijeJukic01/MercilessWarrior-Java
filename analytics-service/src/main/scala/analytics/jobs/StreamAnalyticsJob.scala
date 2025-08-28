package analytics.jobs

import analytics.jobs.schemas.EventSchemas
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql.streaming.Trigger

/**
 * Manages the real-time processing of game events from Kafka using Spark Structured Streaming.
 *
 * This job is responsible for the core ETL (Extract, Transform, Load) pipeline for game analytics.
 * <p>
 * It performs the following steps: <br>
 * 1. Extract: Connects to a Kafka cluster and consumes raw Avro-encoded events from specified topics. <br>
 * 2. Transform: Deserializes the binary Avro messages into structured DataFrames, enriches the data <br>
 * 3. Load: Writes the processed, structured data into a partitioned Parquet-based data lake for analytical querying. <br>
 * <p>
 * The job is designed to be fault-tolerant, utilizing Spark's checkpointing mechanism to ensure exactly-once processing semantics and recover from failures.
 *
 * @param spark The active SparkSession, which serves as the entry point to all Spark functionality.
 */
class StreamAnalyticsJob(
                          spark: SparkSession,
                          kafkaBootstrapServers: String = "kafka:9092",
                          outputPath: String = "data/lake/shop_transactions",
                          checkpointPath: String = "data/checkpoints/shop_transactions"
                        ) extends Logging {

  /**
   * Starts and runs the streaming query.
   *
   * This method defines the streaming DataFrame, applies all transformations, and starts the query that writes data to the sink (the data lake).
   * It then blocks the execution thread with `awaitTermination` to keep the stream running indefinitely.
   */
  def run(): Unit = {
    import spark.implicits._

    logInfo("Starting Game Analytics Stream Job...")

    // Extract
    val kafkaDf = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", "shop_transactions")
      .option("startingOffsets", "earliest")
      .load()

    // Transform
    val shopDf = kafkaDf
      .where("topic = 'shop_transactions'")
      // Skipping magic bytes + schema ID
      .select(expr("substring(value, 6)") as "data")
      .select(from_avro($"data", EventSchemas.shopTransactionSchema) as "shop_data")
      .select("shop_data.*")
      .withColumn("event_date", to_date(from_unixtime($"timestamp" / 1000)))

    // Load
    val query = shopDf.writeStream
      .trigger(Trigger.ProcessingTime("1 second"))
      .format("parquet")
      .option("path", outputPath)
      .option("checkpointLocation", checkpointPath)
      .partitionBy("event_date", "transactionType")
      .start()

    logInfo("Streaming query started. Writing to data lake...")
    query.awaitTermination()
  }
}