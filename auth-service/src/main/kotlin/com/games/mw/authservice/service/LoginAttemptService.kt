package com.games.mw.authservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * Service for tracking login attempts and blocking users after too many failed attempts.
 * Uses Redis to store attempt counts and block durations.
 *
 * @property redisTemplate Redis template for storing attempt data.
 */
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

    /**
     * Resets the failed login attempts for the given key (username).
     *
     * @param key The username or identifier.
     */
    fun loginSucceeded(key: String) {
        redisTemplate.delete(getKey(key))
    }

    /**
     * Increments the failed login attempts for the given key and sets block duration if first failure.
     *
     * @param key The username or identifier.
     */
    fun loginFailed(key: String) {
        val attemptsKey = getKey(key)
        val attempts = redisTemplate.opsForValue().increment(attemptsKey)
        if (attempts == 1L) {
            redisTemplate.expire(attemptsKey, blockDurationMinutes, TimeUnit.MINUTES)
        }
    }

    /**
     * Checks if the user is currently blocked due to too many failed attempts.
     *
     * @param key The username or identifier.
     * @return True if blocked, false otherwise.
     */
    fun isBlocked(key: String): Boolean {
        val attempts = redisTemplate.opsForValue().get(getKey(key))
        return attempts != null && attempts.toInt() >= maxAttempts
    }
}