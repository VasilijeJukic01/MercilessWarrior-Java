package com.games.mw.multiplayerservice.session

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.games.mw.multiplayerservice.ws.*
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
    private val players = ConcurrentHashMap<String, PlayerInfo>()
    private val objectMapper = jacksonObjectMapper()

    /*
        * A scheduled task that performs a health check every 5 seconds to ensure that all players are still connected.
        * If a player has not sent a PONG message within the last 10 seconds, they are considered disconnected.
     */
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    init {
        scheduler.scheduleAtFixedRate(::healthCheck, 10, 10, TimeUnit.SECONDS)
    }

    fun addPlayer(session: WebSocketSession) {
        players[session.id] = PlayerInfo("unknown", "unknown", session)
        sinks[session.id] = Sinks.many().multicast().onBackpressureBuffer()
    }

    fun removePlayer(session: WebSocketSession): PlayerInfo? {
        sinks.remove(session.id)
        return players.remove(session.id)
    }

    /*
        * Called by a player's session when a message is received.
     */
    fun onNext(senderSessionId: String, message: String) {
        try {
            val msg = objectMapper.readValue<MultiplayerMessage>(message)
            when (msg) {
                is PlayerStateDTO -> {
                    if (players[senderSessionId]?.clientId == "unknown") {
                        players[senderSessionId] = PlayerInfo(msg.clientId, msg.username, players[senderSessionId]!!.session)
                    }
                    // Broadcast PlayerState
                    players.forEach { (id, _) ->
                        if (id != senderSessionId) {
                            sinks[id]?.tryEmitNext(message)
                        }
                    }
                }
                is PingDTO -> {
                    players[senderSessionId]?.let { it.lastPingTime = System.currentTimeMillis() }
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

    private fun healthCheck() {
        val now = System.currentTimeMillis()
        val timeout = 15000L
        val timedOutClients = players.values.filter { now - it.lastPingTime > timeout }

        if (timedOutClients.isNotEmpty()) {
            timedOutClients.forEach { playerInfo ->
                println("Client ${playerInfo.clientId} (${playerInfo.session.id}) timed out. No ping received in ${timeout}ms. Closing connection.")
                playerInfo.session.close().subscribe()
            }
        }
    }

    /*
        * This method returns a Flux of WebSocketMessage objects that can be used to send messages to the player.
        * The messages are transformed from String to WebSocketMessage format.
     */
    fun publisherFor(sessionId: String): Flux<WebSocketMessage> {
        val playerSession = players[sessionId]?.session ?: return Flux.empty()
        return sinks[sessionId]?.asFlux()?.map { text ->
            playerSession.textMessage(text)
        } ?: Flux.empty()
    }

    fun broadcastPlayerLeft(clientId: String) {
        val message = PlayerLeftDTO(clientId)
        val jsonMessage = objectMapper.writeValueAsString(message)
        println("Broadcasting PLAYER_LEFT for clientId: $clientId")
        players.forEach { (id, _) ->
            sinks[id]?.tryEmitNext(jsonMessage)
        }
    }

    fun shutdown() {
        scheduler.shutdown()
    }

}

data class PlayerInfo(
    val clientId: String,
    val username: String,
    val session: WebSocketSession,
    @Volatile var lastPingTime: Long = System.currentTimeMillis()
)