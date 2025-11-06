package com.games.mw.gameservice.domain.leaderboard

import com.games.mw.gameservice.domain.leaderboard.LeaderboardService.LeaderboardError
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
    private val leaderboardService: LeaderboardService
) {
    @GetMapping
    fun getLeaderboard(@RequestHeader("Authorization") token: String) : Mono<ResponseEntity<*>> = mono {
        leaderboardService.getLeaderboard(token).fold(
            { error ->
                when(error) {
                    is LeaderboardError.AuthServiceError -> ResponseEntity.status(error.statusCode).body(error.message ?: "Auth service error")
                    is LeaderboardError.Unknown -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.")
                }
            },
            { leaderboard -> ResponseEntity.ok(leaderboard) }
        )
    }
}