package analytics.jobs

import analytics.SparkSessionTestWrapper
import analytics.jobs.schemas.EventSchemas
import com.dimafeng.testcontainers.{ForAllTestContainer, KafkaContainer}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.awaitility.Awaitility._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.utility.DockerImageName

import java.io.ByteArrayOutputStream
import java.nio.file.{Files, Path}
import java.util.Properties
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Using

class StreamAnalyticsJobIntegrationTest extends AnyWordSpec with Matchers with SparkSessionTestWrapper with ForAllTestContainer {

  override val container: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
  private val topic = "shop_transactions"

  "StreamAnalyticsJob" should {
    "consume Avro messages from Kafka, process them, and write to a Parquet data lake" in {
      val tempOutputDir: Path = Files.createTempDirectory("spark-stream-output-")
      val tempCheckpointDir: Path = Files.createTempDirectory("spark-stream-checkpoint-")

      try {
        // Arrange
        val job = new StreamAnalyticsJob(
          spark,
          kafkaBootstrapServers = container.bootstrapServers,
          outputPath = tempOutputDir.toUri.toString,
          checkpointPath = tempCheckpointDir.toUri.toString
        )

        val streamingQueryFuture = Future { job.run() }

        val transaction1 = createShopTransaction("event1", 1698350400000L, 101L, "PlayerOne", "BUY", "item_001", 1, 500, 500)
        val transaction2 = createShopTransaction("event2", 1698350400000L, 102L, "PlayerTwo", "SELL", "item_002", 5, 10, 50)

        val avroRecord1 = serializeAvro(transaction1)
        val avroRecord2 = serializeAvro(transaction2)

        val producerProps = new Properties()
        producerProps.put("bootstrap.servers", container.bootstrapServers)
        producerProps.put("key.serializer", classOf[StringSerializer].getName)
        producerProps.put("value.serializer", classOf[ByteArraySerializer].getName)

        Using(new KafkaProducer[String, Array[Byte]](producerProps)) { producer =>
          producer.send(new ProducerRecord(topic, null, avroRecord1))
          producer.send(new ProducerRecord(topic, null, avroRecord2))
          producer.flush()
        }

        await().atMost(30, TimeUnit.SECONDS).until { () =>
          try {
            val df = spark.read.parquet(tempOutputDir.toUri.toString)
            df.count() == 2
          } catch {
            case _: Exception => false
          }
        }

        // Assert
        spark.streams.active.headOption.foreach { query =>
          query.stop()
          query.awaitTermination(10000)
        }

        val resultDf = spark.read.parquet(tempOutputDir.toUri.toString)
        resultDf.count() should be(2)

        val collectedResults = resultDf
          .select("eventId", "userId", "username", "itemId", "quantity", "unitPrice", "totalPrice")
          .collect()
          .map(row => (
            row.getString(0),
            row.getLong(1),
            row.getString(2),
            row.getString(3),
            row.getInt(4),
            row.getInt(5),
            row.getInt(6)
          ))

        val expectedResults = Set(
          ("event1", 101L, "PlayerOne", "item_001", 1, 500, 500),
          ("event2", 102L, "PlayerTwo", "item_002", 5, 10, 50)
        )

        collectedResults.toSet shouldEqual expectedResults

      } finally {
        // Teardown
        spark.streams.active.foreach(_.stop())
        import java.io.File
        def deleteRecursively(file: File): Unit = {
          if (file.isDirectory) file.listFiles.foreach(deleteRecursively)
          if (file.exists) file.delete
        }
        deleteRecursively(tempOutputDir.toFile)
        deleteRecursively(tempCheckpointDir.toFile)
      }
    }
  }

  private def createShopTransaction(eventId: String, timestamp: Long, userId: Long, username: String, txType: String, itemId: String, quantity: Int, unitPrice: Int, totalPrice: Int): GenericRecord = {
    val schema = new Schema.Parser().parse(EventSchemas.shopTransactionSchema)
    val record = new GenericData.Record(schema)
    record.put("eventId", eventId)
    record.put("timestamp", timestamp)
    record.put("userId", userId)
    record.put("username", username)
    val txTypeEnum = new GenericData.EnumSymbol(schema.getField("transactionType").schema(), txType)
    record.put("transactionType", txTypeEnum)
    record.put("itemId", itemId)
    record.put("quantity", quantity)
    record.put("unitPrice", unitPrice)
    record.put("totalPrice", totalPrice)
    record
  }

  private def serializeAvro(record: GenericRecord): Array[Byte] = {
    val writer = new SpecificDatumWriter[GenericRecord](record.getSchema)
    val out = new ByteArrayOutputStream()
    out.write(0)
    out.write(Array[Byte](0, 0, 0, 1))
    val encoder = EncoderFactory.get().binaryEncoder(out, null)
    writer.write(record, encoder)
    encoder.flush()
    out.close()
    out.toByteArray
  }
}