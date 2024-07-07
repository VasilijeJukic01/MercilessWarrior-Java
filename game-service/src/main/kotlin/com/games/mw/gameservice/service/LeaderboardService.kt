package com.games.mw.gameservice.service

import com.games.mw.gameservice.requests.BoardItemDTO
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class LeaderboardService(
    private val settingsService: SettingsService,
    private val webClientBuilder: WebClient.Builder
) {

    fun getLeaderboard(token: String): List<BoardItemDTO> {
        val authServiceClient = webClientBuilder.baseUrl("http://localhost:8081").build()

        val usernames = authServiceClient.get()
            .uri("/auth/usernames")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .bodyToMono(Array<String>::class.java)
            .block()

        return usernames?.map { username ->
            val accountId = authServiceClient.get()
                .uri("/auth/account/$username")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .bodyToMono(Long::class.java)
                .block()

            val settings = settingsService.getSettingsByUserId(accountId!!)
            BoardItemDTO(username, settings?.level!!, settings.exp)
        }?.sortedBy { it.level }?.reversed() ?: emptyList()
    }

}