package com.games.mw.multiplayerservice.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.games.mw.multiplayerservice.ws.*
import org.awaitility.Awaitility.await
import org.hamcrest.core.IsNull.notNullValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import org.awaitility.core.ConditionTimeoutException
import org.junit.jupiter.api.Tag
import reactor.core.publisher.Sinks
import java.net.URI
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Integration tests for the [MultiplayerWebSocketHandler].
 *
 * These tests start on a random port and use a reactive WebSocket client  to simulate real players hosting, joining, and interacting within game sessions.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MultiplayerWebSocketIntegrationTests {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var client: ReactorNettyWebSocketClient
    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        client = ReactorNettyWebSocketClient()
    }

    /**
     * An Awaitility-based extension function for [ConcurrentLinkedQueue] that waits for a non-null item to be available in the queue and then polls it.
     * It is a crucial for sync the test thread with async message reception.
     *
     * @param duration The maximum time to wait for an item.
     * @return The polled item from the queue.
     * @throws ConditionTimeoutException if no item appears within the duration.
     */
    private fun <T> ConcurrentLinkedQueue<T>.pollAwait(duration: Duration = Duration.ofSeconds(5)): T {
        return await()
            .atMost(duration)
            .until({ this.poll() }, notNullValue())
    }

    /**
     * Establishes a connection to a given URI for a client that only needs to receive messages.
     * Incoming messages are placed into the queue.
     *
     * @param uri The WebSocket endpoint URI.
     * @param messageQueue The queue to store received messages.
     */
    private fun connectAndCollect(uri: String, messageQueue: ConcurrentLinkedQueue<String>) {
        client.execute(URI.create(uri)) { session ->
            session.receive()
                .map { it.payloadAsText }
                .doOnNext { messageQueue.add(it) }
                .then()
        }.subscribe()
    }

    /**
     * Establishes a connection that can both send and receive messages.
     * It returns a [Sinks.Many] which the test can use to send messages to the server.
     *
     * @param uri The WebSocket endpoint URI.
     * @param messageQueue The queue to store received messages.
     * @return A [Sinks.Many] instance for sending messages from the test client.
     */
    private fun connectWithSink(uri: String, messageQueue: ConcurrentLinkedQueue<String>): Sinks.Many<String> {
        val sink = Sinks.many().unicast().onBackpressureBuffer<String>()
        client.execute(URI.create(uri)) { session ->
            val output = session.send(sink.asFlux().map { session.textMessage(it) })
            val input = session.receive().map { it.payloadAsText }.doOnNext { messageQueue.add(it) }.then()
            output.and(input)
        }.subscribe()
        return sink
    }

    /**
     * Verifies the complete session creation and joining flow:
     * 1. A host connects to the /host endpoint.
     * 2. The test asserts that the host receives a [SessionCreatedDTO] with a valid session ID.
     * 3. A client connects to the /join/{sessionId} endpoint using the ID.
     * 4. The test asserts that the joiner receives a [SessionJoinedDTO].
     */
    @Test
    fun `host should create a session and joiner should connect successfully`() {
        val hostMessages = ConcurrentLinkedQueue<String>()
        connectAndCollect("ws://localhost:$port/multiplayer/ws/host", hostMessages)

        val message = hostMessages.pollAwait()
        val sessionCreated = objectMapper.readValue<SessionCreatedDTO>(message)
        val sessionId = sessionCreated.sessionId

        assertNotNull(sessionId)
        assertTrue(sessionId.matches(Regex("\\d{6}")))

        val joinerMessages = ConcurrentLinkedQueue<String>()
        connectAndCollect("ws://localhost:$port/multiplayer/ws/join/$sessionId", joinerMessages)

        val joinMessage = joinerMessages.pollAwait()
        val sessionJoined = objectMapper.readValue<SessionJoinedDTO>(joinMessage)
        assertEquals(sessionId, sessionJoined.sessionId)
    }

    /**
     * Tests that when one client sends its state, it is correctly broadcast to all clients in the session:
     * 1. Host connects.
     * 2. Client connects to the session
     * 3. Client sends a [PlayerStateDTO].
     * 4. Asserts that the Host's message queue receives this [PlayerStateDTO].
     * 5. Asserts that the Client's message queue is empty (didn't receive its own broadcast).
     */
    @Test
    fun `should broadcast PlayerState messages to other clients in the session`() {
        // Hosting
        val hostMessages = ConcurrentLinkedQueue<String>()
        connectAndCollect("ws://localhost:$port/multiplayer/ws/host", hostMessages)
        val sessionId = objectMapper.readValue<SessionCreatedDTO>(hostMessages.pollAwait()).sessionId

        // Client Joining
        val joinerMessages = ConcurrentLinkedQueue<String>()
        val joinerSink = connectWithSink("ws://localhost:$port/multiplayer/ws/join/$sessionId", joinerMessages)
        objectMapper.readValue<SessionJoinedDTO>(joinerMessages.pollAwait()) // Consume join message

        // Client Sending State
        val joinerState = PlayerStateDTO("client-joiner", "Joiner", 100.0, 100.0, 1, 1, 0, 0, 1, 0, false)
        joinerSink.tryEmitNext(objectMapper.writeValueAsString(joinerState))

        val receivedMessage = hostMessages.pollAwait()
        assertNotNull(receivedMessage)
        val receivedState = objectMapper.readValue<PlayerStateDTO>(receivedMessage)
        assertEquals("client-joiner", receivedState.clientId)
        assertEquals("Joiner", receivedState.username)

        Thread.sleep(500)
        assertTrue(joinerMessages.isEmpty())
    }

    /**
     * Verifies the server's health check:
     * 1. A client connects to a new session.
     * 2. The client sends a [PingDTO] message.
     * 3. Asserts that the server responds with a [PongDTO].
     */
    @Test
    fun `should handle ping pong health check`() {
        val clientMessages = ConcurrentLinkedQueue<String>()
        val sink = connectWithSink("ws://localhost:$port/multiplayer/ws/host", clientMessages)
        objectMapper.readValue<SessionCreatedDTO>(clientMessages.pollAwait())

        val clientTime = System.currentTimeMillis()
        val pingMessage = PingDTO(clientTime)
        sink.tryEmitNext(objectMapper.writeValueAsString(pingMessage))

        val receivedMessage = clientMessages.pollAwait()
        assertNotNull(receivedMessage)
        val pongMessage = objectMapper.readValue<PongDTO>(receivedMessage)
        assertEquals(clientTime, pongMessage.clientTime)
    }

    /**
     * Tests the session cleanup and notification logic when a player disconnects:
     * 1. A host connects.
     * 2. A client connects.
     * 3. The test verifies that the host receives this initial state message from the client.
     * 4. The client's connection is closed.
     * 5. Asserts that the host then receives a [PlayerLeftDTO] message that has `clientId` of the disconnected player.
     */
    @Test
    fun `should broadcast PlayerLeft message when a player disconnects`() {
        // Hosting
        val hostMessages = ConcurrentLinkedQueue<String>()
        connectAndCollect("ws://localhost:$port/multiplayer/ws/host", hostMessages)
        val sessionId = objectMapper.readValue<SessionCreatedDTO>(hostMessages.pollAwait()).sessionId

        // Client Joining & Sending State
        val joinerSink = Sinks.many().unicast().onBackpressureBuffer<String>()
        var joinerSession: WebSocketSession? = null

        client.execute(URI.create("ws://localhost:$port/multiplayer/ws/join/$sessionId")) { session ->
            joinerSession = session
            session.send(joinerSink.asFlux().map { session.textMessage(it) }).and(session.receive().then())
        }.subscribe()

        await().atMost(Duration.ofSeconds(5)).until { joinerSession != null }

        val joinerState = PlayerStateDTO("client-to-disconnect", "Leaver", 0.0, 0.0, 0, 0, 0, 0, 1, 0, false)
        joinerSink.tryEmitNext(objectMapper.writeValueAsString(joinerState))

        val stateMessage = hostMessages.pollAwait()
        assertNotNull(stateMessage)
        val receivedState = objectMapper.readValue<PlayerStateDTO>(stateMessage)
        assertEquals("client-to-disconnect", receivedState.clientId)

        // Disconnecting
        joinerSession!!.close().block(Duration.ofSeconds(2))

        val leftMessage = hostMessages.pollAwait()
        assertNotNull(leftMessage)
        val playerLeft = objectMapper.readValue<PlayerLeftDTO>(leftMessage)
        assertEquals("client-to-disconnect", playerLeft.clientId)
    }
}