package com.games.mw.authservice.service.outbox

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.games.mw.events.UserCreated
import com.games.mw.authservice.repository.outbox.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * A scheduled service that implements the "message relay" part of the Transactional Outbox Pattern.
 * It periodically polls the `OutboxEvents` table for unpublished events, sends them to Kafka, and marks them as published in a transaction.
 *
 * This ensures "at-least-once" delivery guarantee.
 * If the service crashes after publishing but before updating the database, the event will be re-published on the next run.
 * Consumers must be idempotent to handle potential duplicates.
 *
 * @property outboxEventRepository Repository for accessing outbox events.
 * @property userCreatedKafkaTemplate Kafka template for publishing UserCreatedEvent messages.
 */
@Service
class OutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val userCreatedKafkaTemplate: KafkaTemplate<String, UserCreated>
) {
    private val logger = LoggerFactory.getLogger(OutboxPublisher::class.java)
    private val objectMapper = jacksonObjectMapper()

    @Scheduled(fixedRate = 5000)
    @Transactional
    fun publishEvents() {
        val eventsToPublish = outboxEventRepository.findTop100ByPublishedAtIsNullOrderByCreatedAt()
        if (eventsToPublish.isEmpty()) return

        logger.info("Found ${eventsToPublish.size} events to publish from outbox.")

        for (event in eventsToPublish) {
            try {
                val payloadMap = objectMapper.readValue(event.payload, Map::class.java)

                val userId = (payloadMap["userId"] as Number).toLong()
                val timestamp = (payloadMap["timestamp"] as Number).toLong()

                val userCreatedEvent = UserCreated.newBuilder()
                    .setEventId(payloadMap["eventId"] as String)
                    .setTimestamp(timestamp)
                    .setUserId(userId)
                    .setUsername(payloadMap["username"] as String)
                    .build()

                userCreatedKafkaTemplate.send("user_events", event.aggregateId, userCreatedEvent).get()

                event.publishedAt = Instant.now()
                outboxEventRepository.save(event)
            } catch (e: Exception) {
                logger.error("Failed to publish outbox event with id ${event.id}", e)
                throw e
            }
        }
    }
}