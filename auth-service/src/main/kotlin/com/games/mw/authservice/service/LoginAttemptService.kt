package com.games.mw.authservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class LoginAttemptService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Value("\${login.attempts.max}")
    private val maxAttempts: Int = 5

    private val blockDurationMinutes: Long = 15

    private fun getKey(key: String): String {
        return "login:attempts:$key"
    }

    fun loginSucceeded(key: String) {
        redisTemplate.delete(getKey(key))
    }

    fun loginFailed(key: String) {
        val attemptsKey = getKey(key)
        val attempts = redisTemplate.opsForValue().increment(attemptsKey)
        if (attempts == 1L) {
            redisTemplate.expire(attemptsKey, blockDurationMinutes, TimeUnit.MINUTES)
        }
    }

    fun isBlocked(key: String): Boolean {
        val attempts = redisTemplate.opsForValue().get(getKey(key))
        return attempts != null && attempts.toInt() >= maxAttempts
    }
}