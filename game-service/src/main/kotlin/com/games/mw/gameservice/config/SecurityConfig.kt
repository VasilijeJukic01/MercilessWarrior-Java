package com.games.mw.gameservice.config

import com.games.mw.gameservice.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

// TODO: Handle permissions
@Configuration
@EnableWebSecurity
open class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/game/**").permitAll()
                    .requestMatchers("/items/**").permitAll()
                    .requestMatchers("/leaderboard/**").permitAll()
                    .requestMatchers("/perks/**").permitAll()
                    .requestMatchers("/settings/**").permitAll()
                    .anyRequest().authenticated()
            }
            .headers { headers ->
                // Scripts, styles, and images can only be loaded from the same domain
                headers.contentSecurityPolicy { csp ->
                    csp.policyDirectives("script-src 'self'; style-src 'self'; img-src 'self';")
                }
                // Prevent clickjacking
                headers.frameOptions { frameOptions ->
                    frameOptions.sameOrigin()
                }
                // Prevent MIME sniffing
                headers.httpStrictTransportSecurity { hsts ->
                    hsts.includeSubDomains(true).maxAgeInSeconds(31536000)
                }
            }
            .sessionManagement { sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}