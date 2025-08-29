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
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.test.web.client.postForObject
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.DirtiesContext

@Tag("integration")
class AuthControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var restTemplate: TestRestTemplate
    @Autowired private lateinit var redisTemplate: RedisTemplate<String, String>
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
        redisTemplate.connectionFactory?.connection?.flushAll()
    }

    // Helper
    private fun registerAndLogin(username: String, password: String): String {
        val registrationRequest = RegistrationRequest(username, password, setOf("USER"))
        restTemplate.postForObject<Long>("/auth/register", registrationRequest)

        val loginRequest = AuthenticationRequest(username, password)
        val response = restTemplate.postForEntity<AuthenticationResponse>("/auth/login", loginRequest)
        return response.body!!.jwt
    }

    @Test
    fun `POST register should create a new user and return user ID`() {
        // Arrange
        val request = RegistrationRequest("integration-test-user", "password123", setOf("USER"))

        // Act
        val response = restTemplate.postForEntity<Long>("/auth/register", request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        val userId = response.body!!

        val foundUserOpt = userRepository.findByIdWithRoles(userId)
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
        restTemplate.postForEntity<Long>("/auth/register", existingUserRequest)

        // Act
        val response = restTemplate.postForEntity<String>("/auth/register", existingUserRequest)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Username is already taken.", response.body)
    }

    @Test
    fun `POST login should return JWT for valid credentials`() {
        // Arrange
        val registrationRequest = RegistrationRequest("login-user", "correct-password", setOf("USER"))
        restTemplate.postForObject<Long>("/auth/register", registrationRequest)

        val loginRequest = AuthenticationRequest("login-user", "correct-password")

        // Act
        val response = restTemplate.postForEntity<AuthenticationResponse>("/auth/login", loginRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertNotNull(response.body?.jwt)
        assertTrue(response.body!!.jwt.isNotEmpty())
    }

    @Test
    fun `POST login should return 401 Unauthorized for invalid credentials`() {
        // Arrange
        val registrationRequest = RegistrationRequest("login-user-fail", "correct-password", setOf("USER"))
        restTemplate.postForObject<Long>("/auth/register", registrationRequest)

        val loginRequest = AuthenticationRequest("login-user-fail", "wrong-password")

        // Act
        val response = restTemplate.postForEntity<String>("/auth/login", loginRequest)

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication failed: Invalid credentials.", response.body)
    }

    @Test
    fun `GET account should return user ID for authenticated user`() {
        // Arrange
        val username = "authed-user"
        val token = registerAndLogin(username, "password123")
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        val entity = HttpEntity<String>(headers)

        // Act
        val response = restTemplate.exchange("/auth/account/$username", HttpMethod.GET, entity, Long::class.java)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        val user = userRepository.findByUsername(username).get()
        assertEquals(user.id, response.body)
    }

    @Test
    fun `GET account should return 403 Forbidden without a token`() {
        // Arrange
        val username = "any-user"

        // Act
        val response = restTemplate.getForEntity("/auth/account/$username", String::class.java)

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `GET usernames should return a list of usernames for authenticated user`() {
        // Arrange
        val user1 = "user1"
        val user2 = "user2"
        registerAndLogin(user1, "pass")
        val token = registerAndLogin(user2, "pass")

        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        val entity = HttpEntity<String>(headers)

        // Act
        val response = restTemplate.exchange("/auth/usernames", HttpMethod.GET, entity, List::class.java)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.contains(user1))
        assertTrue(response.body!!.contains(user2))
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
            val response = restTemplate.postForEntity<String>("/auth/login", loginRequest)
            assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode, "Attempt $i should be UNAUTHORIZED")
        }

        val blockedResponse = restTemplate.postForEntity<String>("/auth/login", loginRequest)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, blockedResponse.statusCode)
        assertEquals("Too many login attempts.", blockedResponse.body)
    }

    @Test
    fun `RateLimitingFilter should return 429 Too Many Requests after exceeding limit`() {
        // Arrange
        val request = RegistrationRequest("rate-limit-test", "password", setOf("USER"))

        // Act & Assert
        for (i in 1..100) {
            val response = restTemplate.postForEntity<String>("/auth/register", request.copy(username = "user-$i"))
            assertEquals(HttpStatus.OK, response.statusCode, "Request $i should be OK")
        }

        val blockedResponse = restTemplate.postForEntity<String>("/auth/register", request.copy(username = "user-101"))
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, blockedResponse.statusCode)
        assertEquals("Too many requests. Please try again later.", blockedResponse.body)
    }

}