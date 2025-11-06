package com.games.mw.authservice.config

import com.games.mw.authservice.security.JwtAuthenticationManager
import com.games.mw.authservice.security.JwtServerAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val authenticationManager: JwtAuthenticationManager,
    private val authenticationConverter: JwtServerAuthenticationConverter,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter)

        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .authorizeExchange { authorize ->
                authorize
                    .pathMatchers("/auth/register", "/auth/login").permitAll()
                    .pathMatchers("/auth/account/**").hasAnyRole("ADMIN", "USER")
                    .pathMatchers("/auth/usernames").hasAnyRole("ADMIN", "USER")
                    .anyExchange().authenticated()
            }
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
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