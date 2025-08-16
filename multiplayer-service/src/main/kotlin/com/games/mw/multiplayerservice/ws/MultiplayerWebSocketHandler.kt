package com.games.mw.multiplayerservice.ws

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.games.mw.multiplayerservice.session.SessionManager
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

/**
 * WebSocket handler for multiplayer game sessions.
 * Handles incoming WebSocket connections and manages the bidirectional communication between players.
 *
 * This handler supports two types of connections:
 * 1. Host connection - Creates a new game session
 * 2. Join connection - Connects to an existing game session
 *
 * The handler uses reactive streams to process incoming messages and broadcast them to all players in the same game session.
 * Expected WebSocket URL formats:
 * - `/multiplayer/ws/host`: For hosting a new game session
 * - `/multiplayer/ws/join/{sessionId}`: For joining an existing game session
 *
 * @property sessionManager The manager responsible for creating and retrieving game sessions
 */
@Component
class MultiplayerWebSocketHandler(
    private val sessionManager: SessionManager
) : WebSocketHandler {

    private val objectMapper = jacksonObjectMapper()

    /**
     * Handles incoming WebSocket connections.
     * Determines the action (host or join) based on the URL path and manages the game session accordingly.
     *
     * @param session The WebSocket session representing the connected player
     * @return A Mono that completes when the connection is closed
     */
    override fun handle(session: WebSocketSession): Mono<Void> {
        val path = session.handshakeInfo.uri.path
        val pathSegments = path.split("/")

        val action = pathSegments.getOrNull(3)
        val sessionId = if (action == "join") pathSegments.getOrNull(4) else null

        val gameSession = when (action) {
            "host" -> sessionManager.createSession()
            "join" -> sessionId?.let { sessionManager.getSession(it) }
            else -> null
        }

        if (gameSession == null) {
            println("Invalid session attempt. Action: $action, SessionID: $sessionId. Closing connection.")
            return session.close()
        }

        val initialMessageMono = when (action) {
            "host" -> {
                val message = SessionCreatedDTO(gameSession.id)
                val jsonMessage = objectMapper.writeValueAsString(message)
                println("Sending SESSION_CREATED to host ${session.id}")
                session.send(Mono.just(session.textMessage(jsonMessage)))
            }
            "join" -> {
                val message = SessionJoinedDTO(gameSession.id)
                val jsonMessage = objectMapper.writeValueAsString(message)
                println("Sending SESSION_JOINED to client ${session.id}")
                session.send(Mono.just(session.textMessage(jsonMessage)))
            }
            else -> Mono.empty()
        }

        println("Player connected to session: ${gameSession.id} (ID: ${session.id})")
        gameSession.addPlayer(session)

        /*
            * This is the reactive pipeline for handling the WebSocket connection.
            * 1. session.receive(): Receives a Flux (stream) of incoming messages.
            * 2. map { it.payloadAsText }: Converts each message payload to a String.
            * 3. doOnNext { gameSession.onNext(it) }: For each message, publishes it to our session's sink.
            * 4. doOnTerminate { ... }: When the connection closes, remove the player.
            * 5. then(): Completes the handling of incoming messages.
         */
        val input = session.receive()
            .map { it.payloadAsText }
            .doOnNext {
                gameSession.onNext(session.id, it)
            }
            .doOnTerminate {
                println("Player disconnected from session: ${gameSession.id} (ID: ${session.id})")
                val removedPlayerInfo = gameSession.removePlayer(session)
                if (removedPlayerInfo != null && removedPlayerInfo.clientId != "unknown") {
                    gameSession.broadcastPlayerLeft(removedPlayerInfo.clientId)
                }
            }
            .then()

        /*
            * 1. session.send(): Prepares to send messages back to the client.
            * 2. gameSession.publisher(): Subscribes to the broadcasted messages from our session's sink.
         */
        val output = session.send(gameSession.publisherFor(session.id))

        /*
            * Mono.zip combines the input and output streams. The connection stays open as long as both are active.
            * When one completes (e.g., the client disconnects), the other is terminated.
         */
        return initialMessageMono.then(Mono.zip(input, output).then())
    }
}