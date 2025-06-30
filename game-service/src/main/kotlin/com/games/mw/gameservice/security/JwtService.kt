package com.games.mw.gameservice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    fun validateToken(token: String): Boolean {
        return !isTokenExpired(token)
    }

    fun extractUserId(token: String): Long {
        return extractAllClaims(token).get("userId", Number::class.java).toLong()
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractRoles(token: String): List<SimpleGrantedAuthority> {
        val claims = extractAllClaims(token)
        @Suppress("UNCHECKED_CAST")
        val roles = claims.get("roles", List::class.java) as? List<String> ?: emptyList()
        return roles.map { SimpleGrantedAuthority("ROLE_$it") }
    }

    private fun extractExpiration(token: String): Date {
        return extractAllClaims(token).expiration
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractAllClaims(token: String): Claims {
        val actualToken = token.replace("Bearer", "").trim()
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(actualToken).body
    }
}