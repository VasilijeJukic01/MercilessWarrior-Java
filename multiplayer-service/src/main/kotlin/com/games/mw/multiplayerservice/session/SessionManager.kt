package com.games.mw.multiplayerservice.session

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the lifecycle and storage of game sessions in the multiplayer service.
 * Provides functionality for creating new sessions and retrieving existing ones.
 * Sessions are stored in a thread-safe concurrent hash map.
 *
 * Each session is identified by a unique 6-digit ID.
 */
@Component
class SessionManager {
    private val sessions = ConcurrentHashMap<String, GameSession>()

    fun createSession(): GameSession {
        val sessionId = generateSessionId()
        val session = GameSession(sessionId)
        sessions[sessionId] = session
        println("Created session: $sessionId")
        return session
    }

    fun getSession(sessionId: String): GameSession? {
        return sessions[sessionId]
    }

    private fun generateSessionId(): String {
        return (100000..999999).random().toString()
    }
}