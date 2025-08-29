package com.games.mw.gameservice.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.Date

object JwtTestUtil {

    private const val secretKey = "sDsBs0eI2DaX7bUp9c9tIZc6ddvV/Isfb0oFynZM+ZRFtVafU6QonqCFSmLt6IbbmqZSUFxr1KVL96ArNTMVxA=="
    private const val expirationTime = 1000 * 60 * 60

    fun generateToken(userId: Long, username: String, roles: List<String>): String {
        val claims = Jwts.claims().setSubject(username)
        claims["userId"] = userId
        claims["roles"] = roles

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }
}