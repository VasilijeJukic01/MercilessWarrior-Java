package com.games.mw.gameservice.domain.leaderboard

import com.games.mw.gameservice.domain.leaderboard.LeaderboardService.LeaderboardError
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
    private val leaderboardService: LeaderboardService
) {
    @GetMapping
    suspend fun getLeaderboard(@RequestHeader("Authorization") token: String) : ResponseEntity<*> {
        return leaderboardService.getLeaderboard(token).fold(
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