package com.games.mw.authservice.request

data class AuthenticationRequest(
    val username: String,
    val password: String
)