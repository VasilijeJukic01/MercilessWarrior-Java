package com.games.mw.authservice

import com.games.mw.authservice.controller.AuthController
import com.games.mw.authservice.request.AuthenticationRequest
import com.games.mw.authservice.request.RegisterRequest
import com.games.mw.authservice.security.JwtService
import com.games.mw.authservice.service.UserDetailsServiceImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import com.games.mw.authservice.model.*
import com.games.mw.authservice.repository.*
import com.games.mw.authservice.request.AuthenticationResponse
import com.games.mw.authservice.service.LoginAttemptService
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verify
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*

class AuthControllerTests {

    @InjectMocks
    private lateinit var authController: AuthController

    @Mock private lateinit var authenticationManager: AuthenticationManager
    @Mock private lateinit var passwordEncoder: PasswordEncoder
    @Mock private lateinit var userDetailsService: UserDetailsServiceImpl
    @Mock private lateinit var jwtService: JwtService
    @Mock private lateinit var loginAttemptService: LoginAttemptService
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var roleRepository: RoleRepository
    @Mock private lateinit var userRoleRepository: UserRoleRepository

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun registerUser_success_test() {
        // Arrange
        val request = RegisterRequest("testuser", "password", setOf("USER"))

        `when`(userRepository.findByUsername(request.username)).thenReturn(Optional.empty())
        `when`(roleRepository.findByName("USER")).thenReturn(Optional.of(Role(1, "USER")))
        `when`(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")

        val savedUser = User(username = request.username, password = "encodedPassword")
        `when`(userRepository.save(savedUser)).thenReturn(savedUser)

        val userRole = UserRole(user = savedUser, role = Role(1, "USER"))
        `when`(userRoleRepository.save(userRole)).thenReturn(userRole)

        // Act
        val response = authController.registerUser(request)

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("User registered successfully", response.body)
    }

    @Test
    fun registerUser_username_taken_test() {
        // Arrange
        val request = RegisterRequest("testuser", "password", setOf("USER"))

        `when`(userRepository.findByUsername(request.username)).thenReturn(Optional.of(User(1, "testuser", "password")))

        // Act
        val response = authController.registerUser(request)

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Username is already taken", response.body)
    }

    @Test
    fun registerUser_role_not_found_test() {
        // Arrange
        val request = RegisterRequest("testuser", "password", setOf("USER"))

        `when`(userRepository.findByUsername(request.username)).thenReturn(Optional.empty())
        `when`(roleRepository.findByName("USER")).thenReturn(Optional.empty())

        // Act
        val exception = assertThrows<RuntimeException> {
            authController.registerUser(request)
        }

        // Assert
        Assertions.assertEquals("Role not found: USER", exception.message)
    }

    @Test
    fun createAuthenticationToken_test_success() {
        // Arrange
        val authenticationRequest = AuthenticationRequest("testuser", "password")

        `when`(loginAttemptService.isBlocked(authenticationRequest.username)).thenReturn(false)

        `when`(authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
        )).thenReturn(UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password))

        val userDetails = org.springframework.security.core.userdetails.User("testuser", "password", emptyList())
        `when`(userDetailsService.loadUserByUsername(authenticationRequest.username)).thenReturn(userDetails)

        `when`(jwtService.generateToken(userDetails)).thenReturn("generatedToken")

        // Act
        val response = authController.createAuthenticationToken(authenticationRequest)

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        if (response.body is AuthenticationResponse) {
            Assertions.assertEquals("generatedToken", (response.body as AuthenticationResponse).jwt)
        }

        verify(loginAttemptService).loginSucceeded(authenticationRequest.username)
    }

    @Test
    fun createAuthenticationToken_test_failed() {
        // Arrange
        val authenticationRequest = AuthenticationRequest("testuser", "password")

        `when`(loginAttemptService.isBlocked(authenticationRequest.username)).thenReturn(false)

        `when`(authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
        )).thenThrow(RuntimeException("Authentication failed"))

        // Act
        val response = authController.createAuthenticationToken(authenticationRequest)

        // Assert
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals("Authentication failed", response.body)
    }

    @Test
    fun createAuthenticationToken_test_user_not_found() {
        // Arrange
        val authenticationRequest = AuthenticationRequest("testuser", "password")

        `when`(authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
        )).thenReturn(UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password))

        `when`(userDetailsService.loadUserByUsername(authenticationRequest.username)).thenThrow(
            UsernameNotFoundException("User not found with username: ${authenticationRequest.username}")
        )

        // Act
        val exception = assertThrows<UsernameNotFoundException> {
            authController.createAuthenticationToken(authenticationRequest)
        }

        // Assert
        Assertions.assertEquals("User not found with username: testuser", exception.message)
    }

}
