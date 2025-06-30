package com.games.mw.authservice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    private val expirationMs: Long = 1000 * 60 * 60 * 10 // 10 hours

    fun generateToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Any>()
        claims["roles"] = userDetails.authorities.map { it.authority.replace("ROLE_", "") }
        if (userDetails is CustomUserDetails) {
            claims["userId"] = userDetails.id
        }

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
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