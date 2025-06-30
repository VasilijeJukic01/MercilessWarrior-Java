package com.games.mw.authservice

import arrow.core.left
import arrow.core.right
import com.games.mw.authservice.controller.AuthController
import com.games.mw.authservice.request.AuthenticationRequest
import com.games.mw.authservice.request.RegistrationRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.http.HttpStatus
import com.games.mw.authservice.model.*
import com.games.mw.authservice.request.AuthenticationResponse
import com.games.mw.authservice.service.AuthService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AuthServiceTests {

    @InjectMocks
    private lateinit var authController: AuthController

    @Mock
    private lateinit var authService: AuthService

    @Test
    fun `registerUser success test`() = runBlocking {
        // Arrange
        val request = RegistrationRequest("testuser", "password", setOf("USER"))
        val mockUser = User(id = 1L, username = "testuser", password = "encodedPassword")
        whenever(authService.registerUser(request)).thenReturn(mockUser.right())

        // Act
        val response = authController.registerUser(request)

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(1L, response.body)
    }

    @Test
    fun `registerUser username taken test`() = runBlocking {
        // Arrange
        val request = RegistrationRequest("testuser", "password", setOf("USER"))
        whenever(authService.registerUser(request)).thenReturn(AuthService.RegistrationError.UsernameTaken.left())

        // Act
        val response = authController.registerUser(request)

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Username is already taken.", response.body)
    }

    @Test
    fun `registerUser role not found test`() = runBlocking {
        // Arrange
        val request = RegistrationRequest("testuser", "password", setOf("INVALID_ROLE"))
        whenever(authService.registerUser(request)).thenReturn(AuthService.RegistrationError.RoleNotFound("INVALID_ROLE").left())

        // Act
        val response = authController.registerUser(request)

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Role not found: INVALID_ROLE.", response.body)
    }

    @Test
    fun `createAuthenticationToken success test`() {
        // Arrange
        val authenticationRequest = AuthenticationRequest("testuser", "password")
        val authenticationResponse = AuthenticationResponse("generatedToken")
        whenever(authService.loginUser(authenticationRequest)).thenReturn(authenticationResponse.right())

        // Act
        val response = authController.createAuthenticationToken(authenticationRequest)

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(authenticationResponse, response.body)
    }

    @Test
    fun `createAuthenticationToken invalid credentials test`() {
        // Arrange
        val authenticationRequest = AuthenticationRequest("testuser", "wrongpassword")
        whenever(authService.loginUser(authenticationRequest)).thenReturn(AuthService.LoginError.InvalidCredentials.left())

        // Act
        val response = authController.createAuthenticationToken(authenticationRequest)

        // Assert
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals("Authentication failed: Invalid credentials.", response.body)
    }

    @Test
    fun `createAuthenticationToken too many attempts test`() {
        // Arrange
        val authenticationRequest = AuthenticationRequest("blockeduser", "password")
        whenever(authService.loginUser(authenticationRequest)).thenReturn(AuthService.LoginError.TooManyAttempts.left())

        // Act
        val response = authController.createAuthenticationToken(authenticationRequest)

        // Assert
        Assertions.assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
        Assertions.assertEquals("Too many login attempts.", response.body)
    }

    @Test
    fun `getUserIdByName success`() {
        val username = "testuser"
        val userId = 123L
        val token = "dummyToken"
        whenever(authService.getUserIdByName(username)).thenReturn(userId.right())

        val response = authController.getUserIdByName(username, token)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(userId, response.body)
    }

    @Test
    fun `getUserIdByName user not found`() {
        val username = "nonexistentuser"
        val token = "dummyToken"
        whenever(authService.getUserIdByName(username)).thenReturn(AuthService.UserAccessError.UserNotFound.left())

        val response = authController.getUserIdByName(username, token)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("User not found.", response.body)
    }

    @Test
    fun `getAllUsernames success`() {
        val usernames = listOf("user1", "user2")
        val token = "dummyToken"
        whenever(authService.getAllUsernames()).thenReturn(usernames)

        val response = authController.getAllUsernames(token)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(usernames, response.body)
    }
}
