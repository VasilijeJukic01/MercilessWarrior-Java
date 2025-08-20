package com.games.mw.multiplayerservice.ws

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SessionCreatedDTO::class, name = "SESSION_CREATED"),
    JsonSubTypes.Type(value = SessionJoinedDTO::class, name = "SESSION_JOINED"),
    JsonSubTypes.Type(value = PlayerLeftDTO::class, name = "PLAYER_LEFT"),
    JsonSubTypes.Type(value = PlayerStateDTO::class, name = "PLAYER_STATE"),
    JsonSubTypes.Type(value = PingDTO::class, name = "PING"),
    JsonSubTypes.Type(value = PongDTO::class, name = "PONG"),
    JsonSubTypes.Type(value = ChatMessageDTO::class, name = "CHAT_MESSAGE")
)
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class MultiplayerMessage

data class SessionCreatedDTO(val sessionId: String) : MultiplayerMessage()

data class SessionJoinedDTO(val sessionId: String) : MultiplayerMessage()

data class PlayerLeftDTO(val clientId: String) : MultiplayerMessage()

data class PingDTO(val clientTime: Long) : MultiplayerMessage()

data class PongDTO(val clientTime: Long) : MultiplayerMessage()

data class PlayerStateDTO(
    val clientId: String,
    val username: String,
    val x: Double,
    val y: Double,
    val levelI: Int,
    val levelJ: Int,
    val animState: Int,
    val animIndex: Int,
    val flipSign: Int,
    val flipCoefficient: Int,
    val isTransformed: Boolean
) : MultiplayerMessage()

data class ChatMessageDTO(
    val username: String,
    val content: String,
    val timestamp: Long
) : MultiplayerMessage()