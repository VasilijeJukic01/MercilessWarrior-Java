package com.games.mw.authservice.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

/**
 * Filter that applies rate limiting per client IP using Redis.
 * Each request is tracked in a sorted set with timestamps, and requests exceeding the configured limit within the time window are rejected.
 *
 * @property redisTemplate RedisTemplate for Redis operations.
 * @property requests Maximum allowed requests per window.
 * @property windowInSeconds Time window for rate limiting in seconds.
 */
@Component
class RateLimitingFilter(
    private val redisTemplate: RedisTemplate<String, String>
) : OncePerRequestFilter() {

    @Value("\${rate.limit.requests}")
    private val requests: Long = 100
    @Value("\${rate.limit.window-seconds}")
    private val windowInSeconds: Long = 60

    companion object {
        private const val RATE_LIMIT_KEY_PREFIX = "rate-limit:"
    }

    /**
     * Filters incoming requests and enforces rate limiting.
     * If the request count exceeds the limit, responds with HTTP 429.
     */
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val key = RATE_LIMIT_KEY_PREFIX + request.remoteAddr

        // Redis transaction
        val sessionCallback = object : SessionCallback<List<Any?>> {
            @Throws(DataAccessException::class)
            override fun <K, V> execute(operations: RedisOperations<K, V>): List<Any?> {
                // We know this callback is executed on a RedisTemplate<String, String>,
                // so we can safely cast the operations object to work with concrete types.
                @Suppress("UNCHECKED_CAST")
                val stringOps = operations as RedisOperations<String, String>

                val now = System.currentTimeMillis()
                val windowStart = now - (windowInSeconds * 1000)

                stringOps.multi()

                // Remove old requests outside the current window
                stringOps.opsForZSet().removeRangeByScore(key, 0.0, windowStart.toDouble())

                // Add the current request with a unique member
                val newMember = now.toString() + "-" + Math.random()
                stringOps.opsForZSet().add(key, newMember, now.toDouble())

                stringOps.opsForZSet().zCard(key)

                // Set expiration for the key to avoid memory leaks
                stringOps.expire(key, Duration.ofSeconds(windowInSeconds))

                return stringOps.exec()
            }
        }

        val results = redisTemplate.execute(sessionCallback)

        // The third command result is the request count
        val requestCount = (results[2] as? Long) ?: (requests + 1)

        if (requestCount <= requests) {
            filterChain.doFilter(request, response)
        }
        else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Too many requests. Please try again later.")
        }
    }
}