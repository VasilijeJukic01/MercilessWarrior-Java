package com.games.mw.gameservice.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.domain.account.PermissionService
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono

typealias PermissionCheckFunction = (Authentication, Long) -> Mono<Boolean>

/**
 * An authorization manager that checks for ownership by inspecting a field within the JSON request body.
 * It relies on a preceding filter (RequestBodyCachingFilter) to have cached the body as a byte array.
 *
 * @param objectMapper To deserialize the request body.
 * @param permissionService The service containing the ownership check logic.
 * @param path The path to the Long ID field within the JSON body (e.g., listOf("settings", "id")).
 */
class RequestBodyAuthorizationManager(
    private val objectMapper: ObjectMapper,
    private val permissionService: PermissionService,
    private val path: List<String>,
    private val permissionCheck: PermissionCheckFunction = permissionService::isOwnerOfRequestBody
) : ReactiveAuthorizationManager<AuthorizationContext> {

    override fun check(authenticationMono: Mono<Authentication>, context: AuthorizationContext): Mono<AuthorizationDecision> {
        val exchange = context.exchange
        val cachedBodyBytes = exchange.getAttribute<ByteArray>(CACHED_REQUEST_BODY_BYTES_ATTR)
            ?: return Mono.just(AuthorizationDecision(false))

        val bodyIdMono = Mono.fromCallable {
            val jsonNode = objectMapper.readTree(cachedBodyBytes)
            var currentNode = jsonNode
            for (fieldName in path) {
                currentNode = currentNode?.get(fieldName)
            }
            currentNode?.asLong()
        }.onErrorResume { Mono.empty() }

        return authenticationMono.zipWith(bodyIdMono)
            .flatMap { tuple ->
                val authentication = tuple.t1
                val ownerId = tuple.t2 ?: return@flatMap Mono.just(false)
                permissionCheck(authentication, ownerId)
            }
            .map { isAuthorized -> AuthorizationDecision(isAuthorized) }
            .defaultIfEmpty(AuthorizationDecision(false))
    }
}