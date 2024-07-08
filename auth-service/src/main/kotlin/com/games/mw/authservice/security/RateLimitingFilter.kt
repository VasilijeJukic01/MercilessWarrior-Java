package com.games.mw.authservice.security

import io.github.bucket4j.*
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration

@Component
class RateLimitingFilter : OncePerRequestFilter() {

    private val buckets = HashMap<String, Bucket>()

    private fun createNewBucket(): Bucket {
        return Bucket4j.builder()
            .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
            .build()
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val ip = request.remoteAddr
        var bucket: Bucket? = buckets[ip]
        if (bucket == null) {
            bucket = createNewBucket()
            buckets[ip] = bucket
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            response.status = 429
            response.writer.write("Too many requests")
        }
    }
}