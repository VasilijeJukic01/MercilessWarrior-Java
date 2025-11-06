package com.games.mw.gameservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.domain.account.PermissionService
import com.games.mw.gameservice.security.CustomReactiveAuthorizationManager
import com.games.mw.gameservice.security.JwtAuthenticationManager
import com.games.mw.gameservice.security.JwtServerAuthenticationConverter
import com.games.mw.gameservice.security.RequestBodyAuthorizationManager
import com.games.mw.gameservice.security.RequestBodyCachingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: JwtAuthenticationManager,
    private val authenticationConverter: JwtServerAuthenticationConverter,
    private val permissionService: PermissionService,
    private val objectMapper: ObjectMapper,
    private val requestBodyCachingFilter: RequestBodyCachingFilter
) {

    private fun hasRole(role: String): ReactiveAuthorizationManager<AuthorizationContext> {
        return ReactiveAuthorizationManager { authentication: Mono<Authentication>, _ ->
            authentication
                .flatMapIterable { it.authorities }
                .any { "ROLE_$role" == it.authority }
                .map { AuthorizationDecision(it) }
        }
    }

    private fun isOwnerOrAdmin(manager: ReactiveAuthorizationManager<AuthorizationContext>): ReactiveAuthorizationManager<AuthorizationContext> {
        return ReactiveAuthorizationManager { authentication: Mono<Authentication>, context: AuthorizationContext ->
            hasRole("ADMIN").check(authentication, context)
                .flatMap { decision ->
                    if (decision.isGranted) Mono.just(decision)
                    else manager.check(authentication, context)
                }
        }
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter)

        val isOwnerByUsernameInPath = isOwnerOrAdmin { auth, context ->
            val username = context.variables["username"] as? String
            auth.map { it.name == username }.map { AuthorizationDecision(it) }
        }

        val isOwnerOfSettings = isOwnerOrAdmin(CustomReactiveAuthorizationManager("settingsId", permissionService::isOwnerOfSettings))
        val isOwnerOfItem = isOwnerOrAdmin(CustomReactiveAuthorizationManager("itemId", permissionService::isOwnerOfItem))
        val isOwnerOfPerk = isOwnerOrAdmin(CustomReactiveAuthorizationManager("perkId", permissionService::isOwnerOfPerk))
        val isOwnerOfUser = isOwnerOrAdmin(CustomReactiveAuthorizationManager("userId", permissionService::isOwnerByUserId))

        val isOwnerOfItemInBody = isOwnerOrAdmin(RequestBodyAuthorizationManager(objectMapper, permissionService, listOf("settings", "id")))
        val isOwnerOfPerkInBody = isOwnerOrAdmin(RequestBodyAuthorizationManager(objectMapper, permissionService, listOf("settings", "id")))
        val isOwnerOfAccountDataInBody = isOwnerOrAdmin(RequestBodyAuthorizationManager(objectMapper, permissionService, listOf("accountId"), permissionService::isOwnerByUserId))
        val isOwnerOfShopTransaction = isOwnerOrAdmin(RequestBodyAuthorizationManager(objectMapper, permissionService, listOf("userId"), permissionService::isOwnerOfTransactionRequest))

        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .authorizeExchange { authorize ->
                authorize
                    // Public
                    .pathMatchers("/leaderboard/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/settings/empty/**").permitAll()

                    // Account
                    .pathMatchers(HttpMethod.GET, "/game/account/{username}").access(isOwnerByUsernameInPath)
                    .pathMatchers(HttpMethod.PUT, "/game/account").access(isOwnerOfAccountDataInBody)

                    // Settings
                    .pathMatchers("/settings/{userId}/**").access(isOwnerOfUser)

                    // Items
                    .pathMatchers("/items/master").hasAnyRole("USER", "ADMIN")
                    .pathMatchers(HttpMethod.GET, "/items/settings/{settingsId}").access(isOwnerOfSettings)
                    .pathMatchers(HttpMethod.POST, "/items/").access(isOwnerOfItemInBody)
                    .pathMatchers(HttpMethod.PUT, "/items/{itemId}").access(isOwnerOfItem)
                    .pathMatchers(HttpMethod.DELETE, "/items/settings/{settingsId}").access(hasRole("ADMIN"))

                    // Perks
                    .pathMatchers(HttpMethod.GET, "/perks/settings/{settingsId}").access(isOwnerOfSettings)
                    .pathMatchers(HttpMethod.POST, "/perks/").access(isOwnerOfPerkInBody)
                    .pathMatchers(HttpMethod.PUT, "/perks/{perkId}").access(isOwnerOfPerk)
                    .pathMatchers(HttpMethod.DELETE, "/perks/settings/{settingsId}").access(hasRole("ADMIN"))

                    // Shop
                    .pathMatchers("/shop/buy", "/shop/sell").access(isOwnerOfShopTransaction)

                    // Default
                    .anyExchange().authenticated()
            }
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterBefore(requestBodyCachingFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { exchange, _ ->
                        Mono.fromRunnable {
                            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                        }
                    }
                    .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
            }
            .build()
    }
}