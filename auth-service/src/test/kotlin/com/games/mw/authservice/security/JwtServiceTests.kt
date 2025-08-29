package com.games.mw.authservice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.util.ReflectionTestUtils

@Tag("unit")
@Suppress("UNCHECKED_CAST")
class JwtServiceTests {

    private lateinit var jwtService: JwtService
    private val testSecretKey = "sDsBs0eI2DaX7bUp9c9tIZc6ddvV/Isfb0oFynZM+ZRFtVafU6QonqCFSmLt6IbbmqZSUFxr1KVL96ArNTMVxA=="

    @BeforeEach
    fun setUp() {
        jwtService = JwtService()
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey)
    }

    @Test
    fun `generateToken should create a valid JWT with correct claims`() {
        // Arrange
        val userId = 123L
        val username = "testuser"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN"))
        val userDetails = CustomUserDetails(userId, username, "password", authorities)

        // Act
        val token = jwtService.generateToken(userDetails)

        // Assert
        assertNotNull(token)

        val claims: Claims = Jwts.parser().setSigningKey(testSecretKey).parseClaimsJws(token).body

        assertEquals(username, claims.subject)
        assertEquals(userId, (claims["userId"] as Number).toLong())

        val roles = claims["roles"] as List<String>
        assertTrue(roles.contains("USER"))
        assertTrue(roles.contains("ADMIN"))
        assertNotNull(claims.expiration)
        assertTrue(claims.expiration.after(java.util.Date()))
    }

    @Test
    fun `validateToken should return true for a valid token`() {
        // Arrange
        val userId = 123L
        val username = "testuser"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val userDetails = CustomUserDetails(userId, username, "password", authorities)
        val token = jwtService.generateToken(userDetails)

        // Act
        val isValid = jwtService.validateToken(token, userDetails)

        // Assert
        assertTrue(isValid)
    }

    @Test
    fun `validateToken should throw ExpiredJwtException for an expired token`() {
        // Arrange
        val expiredJwtService = JwtService()
        ReflectionTestUtils.setField(expiredJwtService, "secretKey", testSecretKey)
        ReflectionTestUtils.setField(expiredJwtService, "expirationMs", -1000L)

        val userDetails = CustomUserDetails(1L, "expiredUser", "password", emptyList())
        val expiredToken = expiredJwtService.generateToken(userDetails)

        // Act & Assert
        assertThrows(ExpiredJwtException::class.java) {
            jwtService.validateToken(expiredToken, userDetails)
        }
    }

    @Test
    fun `validateToken should return false for a token with a different username`() {
        // Arrange
        val userDetails1 = CustomUserDetails(1L, "user1", "password", emptyList())
        val userDetails2 = CustomUserDetails(2L, "user2", "password", emptyList())
        val tokenForUser1 = jwtService.generateToken(userDetails1)

        // Act
        val isValid = jwtService.validateToken(tokenForUser1, userDetails2)

        // Assert
        assertFalse(isValid)
    }

}