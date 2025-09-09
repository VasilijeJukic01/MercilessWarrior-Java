package com.games.mw.authservice.security

import com.games.mw.authservice.service.UserDetailsServiceImpl
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsServiceImpl
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .handle { it, sink ->
                val token = it.credentials as String
                val username = jwtService.extractUsername(token)
                val userDetails = userDetailsService.loadUserByUsername(username)

                if (jwtService.validateToken(token, userDetails)) {
                    val customUserDetails = userDetails as CustomUserDetails
                    sink.next(
                        CustomAuthenticationToken(
                            userId = customUserDetails.id,
                            principal = userDetails.username,
                            credentials = null,
                            authorities = userDetails.authorities
                        )
                    )
                }
                else sink.error(IllegalStateException("Invalid JWT Token"))
            }
    }
}