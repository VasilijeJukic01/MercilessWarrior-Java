package com.games.mw.multiplayerservice.config

import com.games.mw.multiplayerservice.ws.MultiplayerWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

/**
 * Configuration class for WebSocket endpoints.
 * Maps WebSocket URLs to their respective handlers and provides necessary Spring WebSocket configuration.
 *
 * The following endpoints are supported:
 * - /multiplayer/ws/host: For creating a new game session
 * - /multiplayer/ws/join/{sessionId}: For joining an existing game session
 */
@Configuration
class WebSocketConfig {

    @Bean
    fun handlerMapping(webSocketHandler: MultiplayerWebSocketHandler): HandlerMapping {
        val map = mapOf("/multiplayer/ws/**" to webSocketHandler)
        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.order = 1
        handlerMapping.urlMap = map
        return handlerMapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }
}