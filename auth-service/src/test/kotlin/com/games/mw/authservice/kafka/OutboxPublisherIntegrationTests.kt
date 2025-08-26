package com.games.mw.authservice.kafka

import com.games.mw.authservice.IntegrationTestBase
import com.games.mw.authservice.request.RegistrationRequest
import com.games.mw.events.UserCreated
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.time.Duration

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
class OutboxPublisherIntegrationTests : IntegrationTestBase() {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `when user is registered, UserCreated event should be published via Outbox`() {
        // Arrange
        val consumerProps = KafkaTestUtils.consumerProps(kafkaContainer.bootstrapServers, "test-group", "true")
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        consumerProps[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://test-url"
        consumerProps[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
        
        val cf = DefaultKafkaConsumerFactory<String, UserCreated>(consumerProps)
        val consumer = cf.createConsumer()
        consumer.subscribe(listOf("user_events"))

        // Act
        val request = RegistrationRequest("kafka-user", "password", setOf("USER"))
        val response = restTemplate.postForEntity<Long>("/auth/register", request)
        val userId = response.body!!

        val record = KafkaTestUtils.getSingleRecord(consumer, "user_events", Duration.ofSeconds(10))
        consumer.close()

        // Assert
        assertNotNull(record)
        val event = record.value()

        assertNotNull(event)
        assertEquals(userId, event.userId)
        assertEquals("kafka-user", event.username)
        assertNotNull(event.eventId)
    }
}