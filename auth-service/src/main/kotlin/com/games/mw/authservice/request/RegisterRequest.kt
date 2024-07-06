package com.games.mw.authservice.request

data class RegisterRequest(
    val username: String,
    val password: String,
    val roles: Set<String>
)