package com.games.mw.gameservice.domain.leaderboard.requests

data class BoardItemDTO(
    val username: String,
    val level: Int,
    val exp: Int
)