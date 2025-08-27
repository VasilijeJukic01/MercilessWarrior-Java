package com.games.mw.gameservice.service

import arrow.core.left
import arrow.core.right
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.leaderboard.LeaderboardService
import com.games.mw.gameservice.domain.leaderboard.requests.BoardItemDTO
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
class LeaderboardServiceUnitTests {

    @Mock private lateinit var settingsService: SettingsService
    @Mock private lateinit var webClientBuilder: WebClient.Builder
    @Mock private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry
    @Mock private lateinit var webClient: WebClient
    @Mock private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    @Mock private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    @Mock private lateinit var responseSpec: WebClient.ResponseSpec

    private lateinit var leaderboardService: LeaderboardService

    private val testToken = "Bearer token"

    @BeforeEach
    fun setUp() {
        val testRetry = Retry.of("testRetry", RetryConfig.custom<Any>().maxAttempts(1).build())
        val testCircuitBreaker = CircuitBreaker.of("authServiceLeaderboard", CircuitBreakerConfig.ofDefaults())
        whenever(circuitBreakerRegistry.circuitBreaker(eq("authServiceLeaderboard"))).thenReturn(testCircuitBreaker)

        whenever(webClientBuilder.baseUrl(any<String>())).thenReturn(webClientBuilder)
        whenever(webClientBuilder.build()).thenReturn(webClient)
        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)

        leaderboardService = LeaderboardService(settingsService, webClientBuilder, testRetry, circuitBreakerRegistry,  "http://auth-service:8081")
    }

    private fun setupWebClientForUsernames(usernames: List<String>) {
        whenever(requestHeadersUriSpec.uri(eq("/auth/usernames"))).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), eq(testToken))).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<Array<String>>>())).thenReturn(Mono.just(usernames.toTypedArray()))
    }

    private fun setupWebClientForAccountId(username: String, accountId: Long) {
        val specificRequestHeadersSpec: WebClient.RequestHeadersSpec<*> = mock()
        val specificResponseSpec: WebClient.ResponseSpec = mock()

        whenever(requestHeadersUriSpec.uri(eq("/auth/account/$username"))).thenReturn(specificRequestHeadersSpec)
        whenever(specificRequestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), eq(testToken))).thenReturn(specificRequestHeadersSpec)
        whenever(specificRequestHeadersSpec.retrieve()).thenReturn(specificResponseSpec)
        whenever(specificResponseSpec.bodyToMono(any<ParameterizedTypeReference<Long>>())).thenReturn(Mono.just(accountId))
    }

    @Test
    fun `getLeaderboard should return sorted leaderboard when all services succeed`() { runBlocking {
        // Arrange
        val usernames = listOf("userA", "userB", "userC")
        setupWebClientForUsernames(usernames)
        setupWebClientForAccountId("userA", 1L)
        setupWebClientForAccountId("userB", 2L)
        setupWebClientForAccountId("userC", 3L)

        val settingsA = Settings(id = 10L, userId = 1L, level = 10, exp = 1000)
        val settingsB = Settings(id = 20L, userId = 2L, level = 15, exp = 2000)
        val settingsC = Settings(id = 30L, userId = 3L, level = 5, exp = 500)

        whenever(settingsService.getSettingsByUserId(1L)).thenReturn(settingsA.right())
        whenever(settingsService.getSettingsByUserId(2L)).thenReturn(settingsB.right())
        whenever(settingsService.getSettingsByUserId(3L)).thenReturn(settingsC.right())

        // Act
        val result = leaderboardService.getLeaderboard(testToken)

        // Assert
        assertTrue(result.isRight())
        result.map {
            assertEquals(3, it.size)
            assertEquals(BoardItemDTO("userB", 15, 2000), it[0])
            assertEquals(BoardItemDTO("userA", 10, 1000), it[1])
            assertEquals(BoardItemDTO("userC", 5, 500), it[2])
        }
        verify(settingsService, times(3)).getSettingsByUserId(anyLong())
    }}

    @Test
    fun `getLeaderboard should handle auth service failure for usernames`() = runBlocking {
        // Arrange
        whenever(requestHeadersUriSpec.uri(eq("/auth/usernames"))).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), eq(testToken))).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<Array<String>>>())).thenReturn(Mono.error(RuntimeException("Auth service down")))

        // Act
        val result = leaderboardService.getLeaderboard(testToken)

        // Assert
        assertTrue(result.isLeft())
        result.mapLeft {
            assertTrue(it is LeaderboardService.LeaderboardError.Unknown)
        }
        verifyNoInteractions(settingsService)
    }

    @Test
    fun `getLeaderboard should return BoardItemDTO with default values if settings not found`() { runBlocking {
        // Arrange
        val usernames = listOf("newUser")
        setupWebClientForUsernames(usernames)
        setupWebClientForAccountId("newUser", 99L)

        whenever(settingsService.getSettingsByUserId(99L)).thenReturn(SettingsService.SettingsError.SettingsNotFound.left())

        // Act
        val result = leaderboardService.getLeaderboard(testToken)

        // Assert
        assertTrue(result.isRight())
        result.map {
            assertEquals(1, it.size)
            assertEquals(BoardItemDTO("newUser", 0, 0), it[0])
        }
        verify(settingsService).getSettingsByUserId(99L)
    }}
}