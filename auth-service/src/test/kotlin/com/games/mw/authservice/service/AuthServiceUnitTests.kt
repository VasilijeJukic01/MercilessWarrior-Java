package com.games.mw.authservice.service

import arrow.core.left
import arrow.core.right
import com.games.mw.authservice.model.Role
import com.games.mw.authservice.model.User
import com.games.mw.authservice.model.outbox.OutboxEvent
import com.games.mw.authservice.repository.RoleRepository
import com.games.mw.authservice.repository.UserRepository
import com.games.mw.authservice.repository.outbox.OutboxEventRepository
import com.games.mw.authservice.request.AuthenticationRequest
import com.games.mw.authservice.request.AuthenticationResponse
import com.games.mw.authservice.request.RegistrationRequest
import com.games.mw.authservice.security.CustomUserDetails
import com.games.mw.authservice.security.JwtService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceUnitTests {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var roleRepository: RoleRepository
    @Mock private lateinit var outboxEventRepository: OutboxEventRepository
    @Mock private lateinit var passwordEncoder: PasswordEncoder
    @Mock private lateinit var jwtService: JwtService
    @Mock private lateinit var authenticationManager: AuthenticationManager
    @Mock private lateinit var userDetailsService: UserDetailsServiceImpl
    @Mock private lateinit var loginAttemptService: LoginAttemptService
    @Mock private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @InjectMocks
    private lateinit var authService: AuthService

    @Test
    fun `registerUser should succeed for a new user`() { runBlocking {
        // Arrange
        val request = RegistrationRequest("newUser", "password", setOf("USER"))
        val role = Role(1, "USER")
        val savedUser = User(1L, "newUser", "encodedPassword")

        whenever(userRepository.findByUsername(request.username)).thenReturn(Optional.empty())
        whenever(roleRepository.findByName("USER")).thenReturn(Optional.of(role))
        whenever(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any())).thenReturn(savedUser)

        // Act
        val result = authService.registerUser(request)

        // Assert
        assertTrue(result.isRight())
        result.onRight { user ->
            assertEquals(savedUser.id, user.id)
            assertEquals(savedUser.username, user.username)
        }
        verify(userRepository).save(any())
        verify(outboxEventRepository).save(any<OutboxEvent>())
    }}

    @Test
    fun `registerUser should fail if username is taken`() { runBlocking {
        // Arrange
        val request = RegistrationRequest("existingUser", "password", setOf("USER"))
        val existingUser = User(1L, "existingUser", "password")
        whenever(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existingUser))

        // Act
        val result = authService.registerUser(request)

        // Assert
        assertTrue(result.isLeft())
        assertEquals(AuthService.RegistrationError.UsernameTaken.left(), result)
        verify(userRepository, never()).save(any())
        verify(outboxEventRepository, never()).save(any())
    }}

    @Test
    fun `registerUser should fail if role is not found`() { runBlocking {
        // Arrange
        val request = RegistrationRequest("newUser", "password", setOf("INVALID_ROLE"))
        whenever(userRepository.findByUsername(request.username)).thenReturn(Optional.empty())
        whenever(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty())

        // Act
        val result = authService.registerUser(request)

        // Assert
        assertTrue(result.isLeft())
        assertEquals(AuthService.RegistrationError.RoleNotFound("INVALID_ROLE").left(), result)
        verify(userRepository, never()).save(any())
        verify(outboxEventRepository, never()).save(any())
    }}

    @Test
    fun `loginUser should fail if user is blocked`() {
        // Arrange
        val request = AuthenticationRequest("blockedUser", "password")
        whenever(loginAttemptService.isBlocked("blockedUser")).thenReturn(true)

        // Act
        val result = authService.loginUser(request)

        // Assert
        assertTrue(result.isLeft())
        assertEquals(AuthService.LoginError.TooManyAttempts.left(), result)
        verify(authenticationManager, never()).authenticate(any())
    }

    @Test
    fun `loginUser should succeed with valid credentials`() {
        // Arrange
        val request = AuthenticationRequest("testuser", "password")
        val userDetails = CustomUserDetails(1L, "testuser", "encodedPass", emptyList())
        val expectedToken = "jwt-token"
        val mockAuthentication: Authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

        whenever(loginAttemptService.isBlocked(request.username)).thenReturn(false)
        whenever(authenticationManager.authenticate(any())).thenReturn(mockAuthentication)
        whenever(userDetailsService.loadUserByUsername(request.username)).thenReturn(userDetails)
        whenever(jwtService.generateToken(userDetails)).thenReturn(expectedToken)

        // Act
        val result = authService.loginUser(request)

        // Assert
        assertTrue(result.isRight())
        assertEquals(AuthenticationResponse(expectedToken).right(), result)
        verify(loginAttemptService).loginSucceeded(request.username)
    }

    @Test
    fun `loginUser should fail with invalid credentials`() {
        // Arrange
        val request = AuthenticationRequest("testuser", "wrongpassword")
        whenever(loginAttemptService.isBlocked(request.username)).thenReturn(false)
        whenever(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException("Invalid credentials"))

        // Act
        val result = authService.loginUser(request)

        // Assert
        assertTrue(result.isLeft())
        assertEquals(AuthService.LoginError.InvalidCredentials.left(), result)
        verify(loginAttemptService).loginFailed(request.username)
        verify(jwtService, never()).generateToken(any())
    }

}