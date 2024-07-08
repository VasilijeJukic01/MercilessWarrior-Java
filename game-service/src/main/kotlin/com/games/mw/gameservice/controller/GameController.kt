package com.games.mw.gameservice.controller

import com.games.mw.gameservice.requests.AccountDataDTO
import com.games.mw.gameservice.service.GameService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/game")
class GameController(
    private val gameService: GameService
) {

    @GetMapping("/account/{username}")
    fun getAccountData(@PathVariable username: String,
                       @RequestHeader("Authorization") token: String): ResponseEntity<AccountDataDTO> {
        val accountData = gameService.getAccountData(username, token)

        return ResponseEntity.ok(accountData)
    }

    @PutMapping("/account")
    fun updateAccountData(@RequestBody accountDataDTO: AccountDataDTO,
                          @RequestHeader("Authorization") token: String): ResponseEntity<Void> {
        gameService.updateAccountData(accountDataDTO)

        return ResponseEntity.ok().build()
    }

}