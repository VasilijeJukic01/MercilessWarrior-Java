package com.games.mw.gameservice.security

import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono

/**
 * A custom authorization manager that delegates the permission check to a provided function.
 * That function is expected to return a Mono<Boolean>. It allows for asynchronous, non-blocking authorization logic.
 *
 * @param variableName The name of the path variable to extract (e.g., "settingsId", "itemId").
 * @param permissionCheck A function that takes the authentication object and the extracted ID and returns a Mono<Boolean> indicating if access is granted.
 */
class CustomReactiveAuthorizationManager(
    private val variableName: String,
    private val permissionCheck: (Authentication, Long) -> Mono<Boolean>
) : ReactiveAuthorizationManager<AuthorizationContext> {

    override fun check(authenticationMono: Mono<Authentication>, context: AuthorizationContext): Mono<AuthorizationDecision> {
        val variables = context.exchange.request.uri.path.split("/")
        val id = variables.lastOrNull()?.toLongOrNull() ?: return Mono.just(AuthorizationDecision(false))

        return authenticationMono.flatMap { authentication ->
            permissionCheck(authentication, id)
        }.map { isAuthorized ->
            AuthorizationDecision(isAuthorized)
        }.defaultIfEmpty(AuthorizationDecision(false))
    }
}