package com.games.mw.authservice.controller

import com.games.mw.authservice.IntegrationTestBase
import com.games.mw.authservice.model.User
import com.games.mw.authservice.model.UserRole
import com.games.mw.authservice.repository.RoleRepository
import com.games.mw.authservice.repository.UserRepository
import com.games.mw.authservice.request.AuthenticationRequest
import com.games.mw.authservice.request.AuthenticationResponse
import com.games.mw.authservice.request.RegistrationRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@Tag("integration")
@AutoConfigureWebTestClient
class AuthControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var webTestClient: WebTestClient
    @Autowired private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
        redisTemplate.delete(redisTemplate.keys("rate-limit:*")).block()
    }

    // Helper
    private fun registerAndLogin(username: String, password: String): String {
        val registrationRequest = RegistrationRequest(username, password, setOf("USER"))
        webTestClient.post().uri("/auth/register")
            .bodyValue(registrationRequest)
            .exchange()
            .expectStatus().isOk

        val loginRequest = AuthenticationRequest(username, password)
        val response = webTestClient.post().uri("/auth/login")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<AuthenticationResponse>()
            .returnResult()

        return response.responseBody!!.jwt
    }

    @Test
    fun `POST register should create a new user and return user ID`() {
        // Arrange
        val request = RegistrationRequest("integration-test-user", "password123", setOf("USER"))

        // Act
        val result = webTestClient.post().uri("/auth/register")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody<Long>()
            .returnResult()

        val userId = result.responseBody
        assertNotNull(userId)

        // Assert
        val foundUserOpt = userRepository.findByIdWithRoles(userId!!)
        assertTrue(foundUserOpt.isPresent)
        val foundUser = foundUserOpt.get()

        assertEquals("integration-test-user", foundUser.username)
        assertTrue(passwordEncoder.matches("password123", foundUser.password))
        assertEquals(1, foundUser.userRoles.size)
        assertEquals("USER", foundUser.userRoles.first().role.name)
    }

    @Test
    fun `POST register should return 400 Bad Request if username is taken`() {
        // Arrange
        val existingUserRequest = RegistrationRequest("existing-user", "password123", setOf("USER"))
        webTestClient.post().uri("/auth/register").bodyValue(existingUserRequest).exchange().expectStatus().isOk

        // Act & Assert
        webTestClient.post().uri("/auth/register")
            .bodyValue(existingUserRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody<String>()
            .isEqualTo("Username is already taken.")
    }

    @Test
    fun `POST login should return JWT for valid credentials`() {
        // Arrange
        val registrationRequest = RegistrationRequest("login-user", "correct-password", setOf("USER"))
        webTestClient.post().uri("/auth/register").bodyValue(registrationRequest).exchange()

        val loginRequest = AuthenticationRequest("login-user", "correct-password")

        // Act & Assert
        webTestClient.post().uri("/auth/login")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.jwt").isNotEmpty
    }

    @Test
    fun `POST login should return 401 Unauthorized for invalid credentials`() {
        // Arrange
        val registrationRequest = RegistrationRequest("login-user-fail", "correct-password", setOf("USER"))
        webTestClient.post().uri("/auth/register").bodyValue(registrationRequest).exchange()

        val loginRequest = AuthenticationRequest("login-user-fail", "wrong-password")

        // Act & Assert
        webTestClient.post().uri("/auth/login")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody<String>().isEqualTo("Authentication failed: Invalid credentials.")
    }

    @Test
    fun `GET account should return user ID for authenticated user`() {
        // Arrange
        val username = "authed-user"
        val token = registerAndLogin(username, "password123")

        // Act & Assert
        val expectedUserId = userRepository.findByUsername(username).get().id

        webTestClient.get().uri("/auth/account/$username")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<Long>().isEqualTo(expectedUserId!!)
    }

    @Test
    fun `GET account should return 401 Unauthorized without a token`() {
        // Arrange
        val username = "any-user"

        // Act & Assert
        webTestClient.get().uri("/auth/account/$username")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `GET usernames should return a list of usernames for authenticated user`() {
        // Arrange
        val user1 = "user1"
        val user2 = "user2"
        registerAndLogin(user1, "pass")
        val token = registerAndLogin(user2, "pass")

        // Act & Assert
        webTestClient.get().uri("/auth/usernames")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<String>>()
            .value {
                assertTrue(it.contains(user1))
                assertTrue(it.contains(user2))
            }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun `POST login should return 429 Too Many Requests after multiple failed attempts`() {
        // Arrange
        val username = "brute-force-user"
        val password = "password"
        val role = roleRepository.findByName("USER").get()
        val user = User(username = username, password = passwordEncoder.encode(password))
        user.addUserRole(UserRole(user = user, role = role))
        userRepository.save(user)

        val loginRequest = AuthenticationRequest(username, "wrong-password")

        // Act & Assert
        for (i in 1..5) {
            webTestClient.post().uri("/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized
        }

        webTestClient.post().uri("/auth/login")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectBody<String>().isEqualTo("Too many login attempts.")
    }

    @Test
    fun `RateLimitingFilter should return 429 Too Many Requests after exceeding limit`() {
        // Arrange
        val request = RegistrationRequest("rate-limit-test", "password", setOf("USER"))

        // Act & Assert
        for (i in 1..100) {
            webTestClient.post().uri("/auth/register")
                .bodyValue(request.copy(username = "user-$i"))
                .exchange()
                .expectStatus().isOk
        }

        webTestClient.post().uri("/auth/register")
            .bodyValue(request.copy(username = "user-101"))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectBody<String>().isEqualTo("Too many requests. Please try again later.")
    }

}