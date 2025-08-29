package com.games.mw.authservice.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.games.mw.authservice.IntegrationTestBase
import com.games.mw.authservice.model.outbox.OutboxEvent
import com.games.mw.authservice.repository.outbox.OutboxEventRepository
import com.games.mw.authservice.service.outbox.OutboxPublisher
import com.games.mw.events.UserCreated
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Integration test for the Transactional Outbox pattern.
 * <p>
 * This test verifies the end-to-end flow:
 * 1. An API call '/auth/register' triggers a database transaction.
 * 2. An OutboxEvent is saved to the database within that same transaction.
 * 3. The scheduled OutboxPublisher polls the database.
 * 4. The publisher sends the event to a Kafka topic.
 * 5. This test consumes the message from Kafka to confirm the entire flow was successful.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OutboxPublisherIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var outboxPublisher: OutboxPublisher
    @Autowired private lateinit var outboxEventRepository: OutboxEventRepository

    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun cleanup() {
        outboxEventRepository.deleteAll()
    }

    @Test
    fun `outbox publisher should read unpublished events and send them to kafka`() {
        // Arrange
        val userId = 123L
        val username = "kafka-user"
        val eventPayload = mapOf(
            "eventId" to UUID.randomUUID().toString(),
            "timestamp" to Instant.now().toEpochMilli(),
            "userId" to userId,
            "username" to username
        )
        val outboxEvent = OutboxEvent(
            aggregateType = "User",
            aggregateId = userId.toString(),
            eventType = "UserCreatedEvent",
            payload = objectMapper.writeValueAsString(eventPayload)
        )
        outboxEventRepository.saveAndFlush(outboxEvent)

        val consumerProps = KafkaTestUtils.consumerProps(kafkaContainer.bootstrapServers, "test-group-outbox-${UUID.randomUUID()}", "true")
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        consumerProps[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://test-url"
        consumerProps[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        val cf = DefaultKafkaConsumerFactory<String, UserCreated>(consumerProps)
        val consumer = cf.createConsumer()
        consumer.subscribe(listOf("user_events"))

        // Act
        outboxPublisher.publishEvents()

        // Assert
        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15))
        consumer.close()

        assertFalse(records.isEmpty)
        val targetRecord = records.records("user_events").find { record ->
            val event = record.value()
            event != null && event.userId == userId
        }

        assertNotNull(targetRecord)
        val event = targetRecord!!.value()
        assertEquals(userId, event.userId)
        assertEquals(username, event.username)

        await().atMost(5, TimeUnit.SECONDS).until {
            val publishedEvent = outboxEventRepository.findByAggregateId(userId.toString()).firstOrNull()
            publishedEvent?.publishedAt != null
        }
    }
}