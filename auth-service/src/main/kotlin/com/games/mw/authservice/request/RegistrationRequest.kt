package com.games.mw.authservice.request

data class RegistrationRequest(
    val username: String,
    val password: String,
    val roles: Set<String>
)