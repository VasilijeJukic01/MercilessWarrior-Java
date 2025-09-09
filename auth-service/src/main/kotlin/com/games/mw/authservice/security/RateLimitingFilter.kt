package com.games.mw.authservice.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPatternParser // Import this
import reactor.core.publisher.Mono
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
@Order(-100)
class RateLimitingFilter(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) : WebFilter {

    @Value("\${rate.limit.requests}")
    private val requests: Long = 100
    @Value("\${rate.limit.window-seconds}")
    private val windowInSeconds: Long = 60

    companion object {
        private const val RATE_LIMIT_KEY_PREFIX = "rate-limit:"
    }

    private val pattern = PathPatternParser().parse("/auth/**")

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!pattern.matches(exchange.request.path.pathWithinApplication())) {
            return chain.filter(exchange)
        }

        val key = RATE_LIMIT_KEY_PREFIX + (exchange.request.remoteAddress?.address?.hostAddress ?: "unknown")
        val now = System.currentTimeMillis()
        val windowStart = now - (windowInSeconds * 1000)
        val newMember = now.toString() + "-" + Math.random()

        val zSetOps = redisTemplate.opsForZSet()
        val range = Range.from(Range.Bound.inclusive(0.0)).to(Range.Bound.exclusive(windowStart.toDouble()))

        val requestCountMono: Mono<Long> = zSetOps.removeRangeByScore(key, range)
            .then(zSetOps.add(key, newMember, now.toDouble()))
            .then(redisTemplate.expire(key, Duration.ofSeconds(windowInSeconds)))
            .then(zSetOps.count(key, Range.unbounded()))
            .defaultIfEmpty(0L)

        return requestCountMono.flatMap { requestCount ->
            if (requestCount <= requests) {
                chain.filter(exchange)
            }
            else {
                exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                val buffer = exchange.response.bufferFactory().wrap("Too many requests. Please try again later.".toByteArray())
                exchange.response.writeWith(Mono.just(buffer))
            }
        }
    }
}