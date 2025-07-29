package com.games.mw.gameservice.domain.account.requests

data class AccountDataDTO (
    val username: String,
    val accountId: Long,
    val settingsId: Long,
    val spawn: Int,
    val coins: Int,
    val tokens: Int,
    val level: Int,
    val exp: Int,
    val items: List<String>,
    val perks: List<String>
)