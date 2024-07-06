package com.games.mw.authservice.service

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class LoginAttemptService {

    @Value("\${login.attempts.max}")
    private var MAX_ATTEMPT: Int? = null

    private val attemptsCache: LoadingCache<String, Int> = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build(object : CacheLoader<String, Int>() {
            // If the key is not found, create a new entry with 0 attempts
            override fun load(key: String): Int {
                return 0
            }
        })

    fun loginSucceeded(key: String) {
        attemptsCache.invalidate(key)
    }

    fun loginFailed(key: String) {
        var attempts = attemptsCache[key]
        attempts++
        attemptsCache.put(key, attempts)
    }

    fun isBlocked(key: String): Boolean {
        return attemptsCache[key] >= MAX_ATTEMPT!!
    }
}