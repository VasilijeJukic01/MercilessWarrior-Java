package com.games.mw.multiplayerservice.session

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.games.mw.multiplayerservice.ws.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.test.StepVerifier
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Unit tests for the [GameSession] class.
 * Mocks WebSocketSession objects to test the message handling and broadcasting logic in isolation.
 */
@Tag("unit")
class GameSessionTest {

    private lateinit var gameSession: GameSession
    private val objectMapper = jacksonObjectMapper()

    private val mockSession1: WebSocketSession = mock()
    private val mockSession2: WebSocketSession = mock()

    @BeforeEach
    fun setUp() {
        gameSession = GameSession("123456")
        whenever(mockSession1.id).thenReturn("session1")
        whenever(mockSession2.id).thenReturn("session2")
        whenever(mockSession1.textMessage(any())).thenAnswer { createMockMessage(it.getArgument(0)) }
        whenever(mockSession2.textMessage(any())).thenAnswer { createMockMessage(it.getArgument(0)) }
    }

    private fun createMockMessage(text: String): WebSocketMessage {
        val mockMessage: WebSocketMessage = mock()
        whenever(mockMessage.payloadAsText).thenReturn(text)
        return mockMessage
    }

    @Test
    fun `onNext with PlayerStateDTO should update player info and broadcast to others`() {
        // Arrange
        gameSession.addPlayer(mockSession1)
        gameSession.addPlayer(mockSession2)
        val messageQueue2 = ConcurrentLinkedQueue<String>()
        gameSession.publisherFor(mockSession2.id).subscribe { messageQueue2.add(it.payloadAsText) }
        val playerState = PlayerStateDTO("client1", "PlayerOne", 10.0, 20.0, 0, 0, 1, 1, 1, 0, false)
        val message = objectMapper.writeValueAsString(playerState)

        // Act
        gameSession.onNext(mockSession1.id, message)

        // Assert
        assertEquals(message, messageQueue2.poll())
    }

    @Test
    fun `onNext with PingDTO should respond with PongDTO only to sender`() {
        // Arrange
        gameSession.addPlayer(mockSession1)
        gameSession.addPlayer(mockSession2)
        val messageQueue1 = ConcurrentLinkedQueue<String>()
        val messageQueue2 = ConcurrentLinkedQueue<String>()
        gameSession.publisherFor(mockSession1.id).subscribe { messageQueue1.add(it.payloadAsText) }
        gameSession.publisherFor(mockSession2.id).subscribe { messageQueue2.add(it.payloadAsText) }
        val clientTime = System.currentTimeMillis()
        val pingMessage = PingDTO(clientTime)
        val message = objectMapper.writeValueAsString(pingMessage)

        // Act
        gameSession.onNext(mockSession1.id, message)

        // Assert
        val received = messageQueue1.poll()
        assertNotNull(received)
        val pong = objectMapper.readValue<PongDTO>(received)
        assertEquals(clientTime, pong.clientTime)
        assertNull(messageQueue2.poll(), "Session 2 should not receive the pong message")
    }

    @Test
    fun `onNext with ChatMessageDTO should broadcast to all players including sender`() {
        // Arrange
        gameSession.addPlayer(mockSession1)
        gameSession.addPlayer(mockSession2)
        val playerState = PlayerStateDTO("client1", "PlayerOne", 0.0, 0.0, 0, 0, 0, 0, 1, 0, false)
        val messageQueue1 = ConcurrentLinkedQueue<String>()
        val messageQueue2 = ConcurrentLinkedQueue<String>()
        gameSession.publisherFor(mockSession1.id).subscribe { messageQueue1.add(it.payloadAsText) }
        gameSession.publisherFor(mockSession2.id).subscribe { messageQueue2.add(it.payloadAsText) }

        // Act
        gameSession.onNext(mockSession1.id, objectMapper.writeValueAsString(playerState))
        val chatMessage = ChatMessageDTO("PlayerOne", "Hello!", System.currentTimeMillis())
        val message = objectMapper.writeValueAsString(chatMessage)
        gameSession.onNext(mockSession1.id, message)

        // Assert
        assertNotNull(messageQueue2.poll(), "Player 2 should have received the initial state message.")
        val received1 = objectMapper.readValue<ChatMessageDTO>(messageQueue1.poll())
        val received2 = objectMapper.readValue<ChatMessageDTO>(messageQueue2.poll())
        assertNotNull(received1)
        assertEquals("client1", received1.clientId)
        assertNotNull(received2)
        assertEquals("client1", received2.clientId)
    }

    @Test
    fun `removePlayer should trigger broadcastPlayerLeft to remaining players`() {
        // Arrange
        gameSession.addPlayer(mockSession1)
        gameSession.addPlayer(mockSession2)
        val playerState = PlayerStateDTO("client1", "PlayerOne", 0.0, 0.0, 0, 0, 0, 0, 1, 0, false)
        val messageQueue2 = ConcurrentLinkedQueue<String>()
        gameSession.publisherFor(mockSession2.id).subscribe { messageQueue2.add(it.payloadAsText) }

        // Act
        gameSession.onNext(mockSession1.id, objectMapper.writeValueAsString(playerState))
        val removedPlayerInfo = gameSession.removePlayer(mockSession1)
        assertNotNull(removedPlayerInfo)
        if (removedPlayerInfo != null && removedPlayerInfo.clientId != "unknown") {
            gameSession.broadcastPlayerLeft(removedPlayerInfo.clientId)
        }

        // Assert
        val firstMessage = messageQueue2.poll()
        assertNotNull(firstMessage)
        val receivedState = objectMapper.readValue<PlayerStateDTO>(firstMessage)
        assertEquals("client1", receivedState.clientId)

        val secondMessage = messageQueue2.poll()
        assertNotNull(secondMessage)
        val playerLeftMessage = objectMapper.readValue<PlayerLeftDTO>(secondMessage)
        assertEquals("client1", playerLeftMessage.clientId)
    }

    @Test
    fun `publisherFor should return a valid Flux for an existing player`() {
        // Arrange
        gameSession.addPlayer(mockSession1)
        gameSession.addPlayer(mockSession2)
        val publisher = gameSession.publisherFor(mockSession1.id)
        val playerState = PlayerStateDTO("client2", "PlayerTwo", 0.0, 0.0, 0, 0, 0, 0, 1, 0, false)
        val playerStateMessage = objectMapper.writeValueAsString(playerState)

        // Act & Assert
        StepVerifier.create(publisher)
            .expectSubscription()
            .then { gameSession.onNext(mockSession2.id, playerStateMessage) }
            .assertNext { webSocketMessage ->
                assertEquals(playerStateMessage, webSocketMessage.payloadAsText)
            }
            .thenCancel()
            .verify()
    }

    @Test
    fun `publisherFor should return an empty Flux for a non-existent player`() {
        // Act
        val publisher = gameSession.publisherFor("non-existent-session")

        // Assert
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .verify()
    }
}