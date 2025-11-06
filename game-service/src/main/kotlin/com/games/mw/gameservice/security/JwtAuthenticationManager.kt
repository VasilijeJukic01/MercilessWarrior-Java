package com.games.mw.gameservice.security

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val jwtService: JwtService
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .handle { it, sink ->
                val token = it.credentials as String
                if (jwtService.validateToken(token)) {
                    val username = jwtService.extractUsername(token)
                    val roles = jwtService.extractRoles(token)
                    val userId = jwtService.extractUserId(token)
                    sink.next(CustomAuthenticationToken(userId, username, token, roles))
                }
                else sink.error(IllegalStateException("Invalid token"))
            }
    }
}