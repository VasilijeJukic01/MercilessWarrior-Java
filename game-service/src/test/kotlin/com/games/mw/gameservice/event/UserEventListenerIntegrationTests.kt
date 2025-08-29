package com.games.mw.gameservice.event

import com.games.mw.events.UserCreated
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Tag("integration")
class UserEventListenerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var settingsRepository: SettingsRepository

    private lateinit var kafkaTemplate: KafkaTemplate<String, UserCreated>

    @BeforeEach
    fun setup() {
        val producerProps = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to "mock://test-url"
        )
        val producerFactory = DefaultKafkaProducerFactory<String, UserCreated>(producerProps)
        kafkaTemplate = KafkaTemplate(producerFactory)
        settingsRepository.deleteAll()
    }


    @AfterEach
    fun cleanup() {
        settingsRepository.deleteAll()
    }

    @Test
    fun `should consume UserCreated event and create settings`() {
        // Arrange
        val userId = Random().nextLong(1000, 1000000)
        val username = "test-user-$userId"
        val userCreatedEvent = UserCreated.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setTimestamp(Instant.now().toEpochMilli())
            .setUserId(userId)
            .setUsername(username)
            .build()

        // Act
        kafkaTemplate.send("user_events", userId.toString(), userCreatedEvent).get(10, TimeUnit.SECONDS)

        // Assert (wait until the settings are created in the database)
        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            val settings = settingsRepository.findByUserId(userId)
            assertNotNull(settings)
            assertEquals(userId, settings?.userId)
            assertEquals(1, settings?.level)
        }
    }

    @Test
    fun `should handle duplicate UserCreated event gracefully`() {
        // Arrange
        val userId = Random().nextLong(1000, 1000000)
        val username = "test-user-$userId"
        val userCreatedEvent = UserCreated.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setTimestamp(Instant.now().toEpochMilli())
            .setUserId(userId)
            .setUsername(username)
            .build()

        // Act
        kafkaTemplate.send("user_events", userId.toString(), userCreatedEvent).get(10, TimeUnit.SECONDS)
        kafkaTemplate.send("user_events", userId.toString(), userCreatedEvent).get(10, TimeUnit.SECONDS)

        // Assert
        await().atMost(Duration.ofSeconds(5)).until { settingsRepository.findByUserId(userId) != null }
        Thread.sleep(2000)
        val allSettingsForUser = settingsRepository.findAll().filter { it.userId == userId }
        assertEquals(1, allSettingsForUser.size, "Should only have one settings entry for the user despite duplicate events.")
        assertEquals(1, settingsRepository.count(), "Total settings in the repository should be one.")
    }
}