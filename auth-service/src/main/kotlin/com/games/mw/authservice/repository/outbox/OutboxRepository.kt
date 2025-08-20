package com.games.mw.authservice.repository.outbox

import com.games.mw.authservice.model.outbox.OutboxEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OutboxEventRepository : JpaRepository<OutboxEvent, Long> {
    fun findTop100ByPublishedAtIsNullOrderByCreatedAt(): List<OutboxEvent>
}