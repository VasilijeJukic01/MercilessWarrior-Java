package com.games.mw.authservice.security

import com.games.mw.authservice.security.IPUtils.getClientIP
import com.games.mw.authservice.service.LoginAttemptService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationFailureHandler(
    private val loginAttemptService: LoginAttemptService
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val ip = getClientIP(request)
        loginAttemptService.loginFailed(ip)
    }
}

@Component
class CustomAuthenticationSuccessHandler(
    private val loginAttemptService: LoginAttemptService
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val ip = getClientIP(request)
        loginAttemptService.loginSucceeded(ip)
    }
}