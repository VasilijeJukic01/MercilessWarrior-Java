package com.games.mw.gameservice.controller

import com.games.mw.gameservice.requests.AccountDataDTO
import com.games.mw.gameservice.service.GameService.GameError
import com.games.mw.gameservice.service.GameService
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/game")
class GameController(
    private val gameService: GameService
) {

    @GetMapping("/account/{username}")
    fun getAccountData(@PathVariable username: String, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return runBlocking {
            gameService.getAccountData(username, token).fold(
                { error -> handleGameError(error, "Failed to get account data.") },
                { accountData -> ResponseEntity.ok(accountData) }
            )
        }
    }

    @PutMapping("/account")
    fun updateAccountData(@RequestBody accountDataDTO: AccountDataDTO, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return runBlocking {
            gameService.updateAccountData(accountDataDTO).fold(
                { error -> handleGameError(error, "Failed to update account data.") },
                { ResponseEntity.ok().build<Void>() }
            )
        }
    }

    private fun handleGameError(error: GameError, defaultMessage: String): ResponseEntity<String> {
        val (httpStatus, errorMessage) = when (error) {
            is GameError.AuthServiceInteractionError -> error.statusCode to (error.message ?: "Auth service interaction failed.")
            is GameError.SettingsOperationFailed -> HttpStatus.INTERNAL_SERVER_ERROR to "Settings operation failed: ${error.underlyingError}"
            is GameError.ItemOperationFailed -> HttpStatus.INTERNAL_SERVER_ERROR to "Item operation failed: ${error.underlyingError}"
            is GameError.PerkOperationFailed -> HttpStatus.INTERNAL_SERVER_ERROR to "Perk operation failed: ${error.underlyingError}"
            is GameError.UnknownSource -> HttpStatus.INTERNAL_SERVER_ERROR to (error.throwable.message ?: defaultMessage)
        }
        return ResponseEntity.status(httpStatus).body(errorMessage)
    }

}