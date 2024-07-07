package com.games.mw.gameservice.controller

import com.games.mw.gameservice.requests.BoardItemDTO
import com.games.mw.gameservice.service.LeaderboardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
        private val leaderboardService: LeaderboardService
) {
    @GetMapping
    fun getLeaderboard(@RequestHeader("Authorization") token: String) : ResponseEntity<List<BoardItemDTO>> {
        val leaderboard = leaderboardService.getLeaderboard(token)

        return ResponseEntity.ok(leaderboard)
    }
}