package com.games.mw.authservice.config

import com.games.mw.authservice.security.RateLimitingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RateLimitConfig(
    private val rateLimitingFilter: RateLimitingFilter
) {

    @Bean
    fun rateLimitingFilterRegistrationBean(): FilterRegistrationBean<RateLimitingFilter> {
        val registrationBean = FilterRegistrationBean<RateLimitingFilter>()
        registrationBean.filter = rateLimitingFilter
        registrationBean.addUrlPatterns("/auth/*")

        return registrationBean
    }
}