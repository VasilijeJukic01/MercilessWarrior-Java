package com.games.mw.multiplayerservice.session

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap
import com.games.mw.multiplayerservice.ws.MultiplayerMessage
import com.games.mw.multiplayerservice.ws.PingDTO
import com.games.mw.multiplayerservice.ws.PlayerStateDTO
import com.games.mw.multiplayerservice.ws.PongDTO

/**
 * Represents an active game session that manages communication between multiple connected players.
 * This class handles the WebSocket message broadcasting using a reactive approach with Reactor's Sinks.
 *
 * @property id The unique identifier for this game session
 */
class GameSession(
    val id: String
) {

    /*
        * Sinks are used to collect messages from players and broadcast them to all subscribed players.
        * We use a multicast sink to allow multiple players to send messages that will be broadcasted to all players in the session.
     */
    private val sinks = ConcurrentHashMap<String, Sinks.Many<String>>()
    private val players = ConcurrentHashMap<String, WebSocketSession>()
    private val objectMapper = jacksonObjectMapper()

    fun addPlayer(session: WebSocketSession) {
        players[session.id] = session
        sinks[session.id] = Sinks.many().multicast().onBackpressureBuffer()
    }

    fun removePlayer(session: WebSocketSession) {
        players.remove(session.id)
        sinks.remove(session.id)
    }

    /*
        * Called by a player's session when a message is received.
     */
    fun onNext(senderSessionId: String, message: String) {
        try {
            val msg = objectMapper.readValue<MultiplayerMessage>(message)
            when (msg) {
                is PlayerStateDTO -> {
                    // Broadcast PlayerState
                    players.forEach { (id, _) ->
                        if (id != senderSessionId) {
                            sinks[id]?.tryEmitNext(message)
                        }
                    }
                }
                is PingDTO -> {
                    // Received a PING -> Send PONG
                    val pongMessage = PongDTO(msg.clientTime)
                    val jsonPong = objectMapper.writeValueAsString(pongMessage)
                    sinks[senderSessionId]?.tryEmitNext(jsonPong)
                }
                else -> {}
            }
        } catch (e: Exception) {
            println("Could not parse message: $message. Error: ${e.message}")
        }
    }

    /*
        * This method returns a Flux of WebSocketMessage objects that can be used to send messages to the player.
        * The messages are transformed from String to WebSocketMessage format.
     */
    fun publisherFor(sessionId: String): Flux<WebSocketMessage> {
        return sinks[sessionId]?.asFlux()?.map { text ->
            val bufferFactory = players[sessionId]?.bufferFactory() ?: return@map null
            val buffer = bufferFactory.wrap(text.toByteArray())
            WebSocketMessage(WebSocketMessage.Type.TEXT, buffer)
        }?.filter { it != null }?.map { it!! } ?: Flux.empty()
    }

}