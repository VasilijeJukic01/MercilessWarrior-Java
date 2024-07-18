package com.games.mw.gameservice.config

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
open class RetryConfig {

    @Bean
    open fun retryRegistry(): RetryRegistry {
        val retryConfig = RetryConfig.custom<RetryConfig>()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(1000))
            .build()

        return RetryRegistry.of(retryConfig)
    }

    @Bean
    open fun authServiceRetry(retryRegistry: RetryRegistry): Retry {
        return retryRegistry.retry("authService")
    }
}