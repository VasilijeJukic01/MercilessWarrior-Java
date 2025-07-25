package com.games.mw.gameservice.service

import arrow.core.Either
import arrow.core.raise.either
import com.games.mw.gameservice.requests.BoardItemDTO
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody

/**
 * Service for retrieving and assembling leaderboard data.
 *
 * @property settingsService Service for accessing user settings.
 * @property webClientBuilder Builder for creating WebClient instances.
 * @property retry Retry configuration for external calls.
 * @property authServiceCircuitBreaker Circuit breaker for auth service communication.
 */
@Service
class LeaderboardService(
    private val settingsService: SettingsService,
    private val webClientBuilder: WebClient.Builder,
    private val retry: Retry,
    circuitBreakerRegistry: CircuitBreakerRegistry
) {

    sealed interface LeaderboardError {
        data class AuthServiceError(val statusCode: HttpStatus, val message: String?) : LeaderboardError
        data class Unknown(val throwable: Throwable) : LeaderboardError
    }

    private val authServiceCircuitBreaker: CircuitBreaker = circuitBreakerRegistry.circuitBreaker("authServiceLeaderboard")

    /**
     * Fetches all usernames from the authentication service.
     *
     * @param token The authentication token for the request.
     * @return [Either] containing a list of usernames or a [LeaderboardError] on failure.
     */
    private suspend fun fetchUsernames(token: String): Either<LeaderboardError, List<String>> = either {
        val authServiceClient = webClientBuilder.baseUrl("http://auth-service:8081").build()
        try {
            authServiceClient.get()
                .uri("/auth/usernames")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .awaitBody<Array<String>>()
                .toList()
        } catch (e: WebClientResponseException) {
            raise(LeaderboardError.AuthServiceError(HttpStatus.valueOf(e.statusCode.value()), e.responseBodyAsString))
        } catch (e: Exception) {
            raise(LeaderboardError.Unknown(e))
        }
    }

    /**
     * Fetches the account ID for a given username from the authentication service.
     *
     * @param username The username to look up.
     * @param token The authentication token for the request.
     * @return [Either] containing the account ID or a [LeaderboardError] on failure.
     */
    private suspend fun fetchAccountId(username: String, token: String): Either<LeaderboardError, Long> = either {
        val authServiceClient = webClientBuilder.baseUrl("http://auth-service:8081").build()
        try {
            authServiceClient.get()
                .uri("/auth/account/$username")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .awaitBody<Long>()
        } catch (e: WebClientResponseException) {
            raise(LeaderboardError.AuthServiceError(HttpStatus.valueOf(e.statusCode.value()), e.responseBodyAsString))
        } catch (e: Exception) {
            raise(LeaderboardError.Unknown(e))
        }
    }

    /**
     * Retrieves the leaderboard, assembling user data sorted by level.
     *
     * @param token The authentication token for the request.
     * @return [Either] containing a list of [BoardItemDTO]s or a [LeaderboardError] on failure.
     */
    suspend fun getLeaderboard(token: String): Either<LeaderboardError, List<BoardItemDTO>> = either {
        val usernames = retry.executeSuspendFunction {
            authServiceCircuitBreaker.executeSuspendFunction {
                fetchUsernames(token).bind()
            }
        }

        usernames.map { username ->
            val accountId = retry.executeSuspendFunction {
                authServiceCircuitBreaker.executeSuspendFunction {
                    fetchAccountId(username, token).bind()
                }
            }
            val settings = settingsService.getSettingsByUserId(accountId).getOrNull()
            BoardItemDTO(username, settings?.level ?: 0, settings?.exp ?: 0)
        }.sortedByDescending { it.level }
    }
}
