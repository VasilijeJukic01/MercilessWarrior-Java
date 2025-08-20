package com.games.mw.authservice.model.outbox

import jakarta.persistence.*
import java.time.Instant

/**
 * Represents a message to be published to a message broker, stored within the same database
 * as the business entity to ensure transactional consistency (Transactional Outbox Pattern).
 *
 * This entity is created in the same transaction as the main business logic.
 * A separate process then reads from this table and publishes the events, guaranteeing that no
 * event is lost if the service crashes after committing the database transaction but before publishing the event.
 *
 * @property id The unique identifier for the outbox event.
 * @property aggregateType The type of the business entity this event relates to.
 * @property aggregateId The unique ID of the business entity instance.
 * @property eventType A string identifying the type of event.
 * @property payload The serialized event data, typically in JSON format.
 * @property createdAt Timestamp of when the event was created.
 * @property publishedAt Timestamp of when the event was successfully published. A NULL value indicates the event has not been published yet.
 */
@Entity
@Table(name = "OutboxEvents")
data class OutboxEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val aggregateType: String,

    @Column(nullable = false)
    val aggregateId: String,

    @Column(nullable = false)
    val eventType: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    var publishedAt: Instant? = null
)  {
    constructor() : this(null, "", "", "", "", Instant.now(), null)
}