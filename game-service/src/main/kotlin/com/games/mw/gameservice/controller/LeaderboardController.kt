package com.games.mw.gameservice.controller

import com.games.mw.gameservice.service.LeaderboardService.LeaderboardError
import com.games.mw.gameservice.service.LeaderboardService
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
    fun getLeaderboard(@RequestHeader("Authorization") token: String) : ResponseEntity<*> {
        return runBlocking {
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
}