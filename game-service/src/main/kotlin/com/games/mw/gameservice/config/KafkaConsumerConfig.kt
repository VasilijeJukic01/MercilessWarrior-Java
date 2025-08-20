package com.games.mw.gameservice.config

import com.games.mw.events.UserCreated
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

/**
 * Configures the Kafka consumer factories and listener containers for the application.
 */
@Configuration
class KafkaConsumerConfig {

    private val logger = LoggerFactory.getLogger(KafkaConsumerConfig::class.java)

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.properties.schema.registry.url}")
    private lateinit var schemaRegistryUrl: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    /**
     * Creates a Kafka consumer factory for UserCreated event messages.
     * Configures servers, group ID, and Avro-specific deserialization properties.
     */
    @Bean
    fun userCreatedConsumerFactory(): ConsumerFactory<String, UserCreated> {
        val props: MutableMap<String, Any> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        props[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl
        props[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
        return DefaultKafkaConsumerFactory(props)
    }

    /**
     * Creates a listener container factory for handling UserCreated messages.
     *
     * This factory is configured with an error handling strategy:
     * - **Retry:** A failed message will be retried 2 times with a 1-second interval.
     *   This handles transient errors like temporary network issues or database deadlocks.
     * - **Dead Letter Queue (DLQ):** After all retry attempts are exhausted, the failed message is sent to a Dead Letter Queue topic.
     *   This prevents a "poison pill" message from blocking the consumer indefinitely and allows for manual inspection and reprocessing.
     *
     * @param kafkaTemplate A generic KafkaTemplate used by the DeadLetterPublishingRecoverer to send failed messages to the DLQ.
     */
    @Bean
    fun userCreatedKafkaListenerContainerFactory(kafkaTemplate: KafkaTemplate<*, *>): ConcurrentKafkaListenerContainerFactory<String, UserCreated> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, UserCreated>()
        factory.consumerFactory = userCreatedConsumerFactory()

        // Configure Retry and DLQ
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate as KafkaTemplate<Any, Any>)
        val errorHandler = DefaultErrorHandler(recoverer, FixedBackOff(1000L, 2L))
        errorHandler.setRetryListeners(
            { record, ex, deliveryAttempt ->
                logger.warn("Failed to process message. Attempt {}/3. Record: {}, Error: {}", deliveryAttempt, record, ex.message)
            }
        )
        factory.setCommonErrorHandler(errorHandler)

        return factory
    }
}