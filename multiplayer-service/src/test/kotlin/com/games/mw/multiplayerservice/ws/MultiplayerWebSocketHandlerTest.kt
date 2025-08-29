package com.games.mw.multiplayerservice.ws

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.games.mw.multiplayerservice.session.GameSession
import com.games.mw.multiplayerservice.session.SessionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.URI

/**
 * Unit tests for the [MultiplayerWebSocketHandler].
 * Mocks the [SessionManager] and [WebSocketSession] to verify routing and session logic.
 */
@Tag("unit")
@ExtendWith(SpringExtension::class)
class MultiplayerWebSocketHandlerTest {

    @Mock private lateinit var sessionManager: SessionManager

    @InjectMocks private lateinit var webSocketHandler: MultiplayerWebSocketHandler

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `handle should create a new session for host URL`() {
        // Arrange
        val mockSession: WebSocketSession = mock()
        val mockHandshakeInfo: HandshakeInfo = mock()
        val mockGameSession: GameSession = mock()
        val mockMessage: WebSocketMessage = mock()

        whenever(mockSession.id).thenReturn("mock-session-id-host")
        whenever(mockSession.handshakeInfo).thenReturn(mockHandshakeInfo)
        whenever(mockHandshakeInfo.uri).thenReturn(URI.create("ws://localhost/multiplayer/ws/host"))
        whenever(sessionManager.createSession()).thenReturn(mockGameSession)
        whenever(mockGameSession.id).thenReturn("123456")
        whenever(mockSession.send(any())).thenReturn(Mono.empty())
        whenever(mockSession.receive()).thenReturn(Flux.empty())
        whenever(mockGameSession.publisherFor(any())).thenReturn(Flux.empty())
        whenever(mockSession.textMessage(any())).thenReturn(mockMessage)
        whenever(mockMessage.payloadAsText).thenReturn(
            objectMapper.writeValueAsString(SessionCreatedDTO("123456"))
        )

        // Act
        webSocketHandler.handle(mockSession).subscribe()

        // Assert
        verify(sessionManager).createSession()
        verify(mockGameSession).addPlayer(mockSession)

        val messageCaptor = argumentCaptor<Mono<WebSocketMessage>>()
        verify(mockSession).send(messageCaptor.capture())

        StepVerifier.create(messageCaptor.firstValue)
            .assertNext {
                val sessionCreated = objectMapper.readValue<SessionCreatedDTO>(it.payloadAsText)
                assertEquals("123456", sessionCreated.sessionId)
            }
            .verifyComplete()
    }

    @Test
    fun `handle should join an existing session for join URL`() {
        // Arrange
        val sessionId = "654321"
        val mockSession: WebSocketSession = mock()
        val mockHandshakeInfo: HandshakeInfo = mock()
        val mockGameSession: GameSession = mock()
        val mockMessage: WebSocketMessage = mock()

        whenever(mockSession.id).thenReturn("mock-session-id-join")
        whenever(mockSession.handshakeInfo).thenReturn(mockHandshakeInfo)
        whenever(mockHandshakeInfo.uri).thenReturn(URI.create("ws://localhost/multiplayer/ws/join/$sessionId"))
        whenever(sessionManager.getSession(sessionId)).thenReturn(mockGameSession)
        whenever(mockGameSession.id).thenReturn(sessionId)
        whenever(mockSession.send(any())).thenReturn(Mono.empty())
        whenever(mockSession.receive()).thenReturn(Flux.empty())
        whenever(mockGameSession.publisherFor(any())).thenReturn(Flux.empty())
        whenever(mockSession.textMessage(any())).thenReturn(mockMessage)
        whenever(mockMessage.payloadAsText).thenReturn(
            objectMapper.writeValueAsString(SessionJoinedDTO(sessionId))
        )

        // Act
        webSocketHandler.handle(mockSession).subscribe()

        // Assert
        verify(sessionManager).getSession(sessionId)
        verify(mockGameSession).addPlayer(mockSession)

        val messageCaptor = argumentCaptor<Mono<WebSocketMessage>>()
        verify(mockSession).send(messageCaptor.capture())

        StepVerifier.create(messageCaptor.firstValue)
            .assertNext {
                val sessionJoined = objectMapper.readValue<SessionJoinedDTO>(it.payloadAsText)
                assertEquals(sessionId, sessionJoined.sessionId)
            }
            .verifyComplete()
    }

    @Test
    fun `handle should close session for invalid join URL`() {
        // Arrange
        val sessionId = "000000"
        val mockSession: WebSocketSession = mock()
        val mockHandshakeInfo: HandshakeInfo = mock()

        whenever(mockSession.handshakeInfo).thenReturn(mockHandshakeInfo)
        whenever(mockHandshakeInfo.uri).thenReturn(URI.create("ws://localhost/multiplayer/ws/join/$sessionId"))
        whenever(sessionManager.getSession(sessionId)).thenReturn(null)
        whenever(mockSession.close()).thenReturn(Mono.empty())

        // Act
        webSocketHandler.handle(mockSession).subscribe()

        // Assert
        verify(sessionManager).getSession(sessionId)
        verify(mockSession).close()
        verify(sessionManager, never()).createSession()
    }
}