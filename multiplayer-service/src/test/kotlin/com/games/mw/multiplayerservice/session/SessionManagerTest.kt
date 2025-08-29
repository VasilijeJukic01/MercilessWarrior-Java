package com.games.mw.multiplayerservice.session

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Unit tests for the [SessionManager].
 * Verify the core functionality of creating, retrieving, and handling game sessions in isolation.
 */
@Tag("unit")
class SessionManagerTest {

    private lateinit var sessionManager: SessionManager

    @BeforeEach
    fun setUp() {
        sessionManager = SessionManager()
    }

    @Test
    fun `createSession should return a new session with a valid 6-digit ID`() {
        // Act
        val session = sessionManager.createSession()

        // Assert
        assertNotNull(session)
        assertTrue(session.id.matches(Regex("\\d{6}")))
    }

    @Test
    fun `getSession should retrieve an existing session`() {
        // Arrange
        val createdSession = sessionManager.createSession()

        // Act
        val retrievedSession = sessionManager.getSession(createdSession.id)

        // Assert
        assertNotNull(retrievedSession)
        assertEquals(createdSession.id, retrievedSession?.id)
    }

    @Test
    fun `getSession should return null for a non-existent session`() {
        // Act
        val retrievedSession = sessionManager.getSession("000000")

        // Assert
        assertNull(retrievedSession)
    }
}