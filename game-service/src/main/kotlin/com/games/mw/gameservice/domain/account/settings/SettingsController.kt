package com.games.mw.gameservice.domain.account.settings

import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.SettingsService.SettingsError
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/settings")
class SettingsController (
    private val settingsService: SettingsService
) {

    @GetMapping("/{userId}")
    fun getSettingsByUserId(@PathVariable userId: Long): Mono<ResponseEntity<*>> = mono {
        settingsService.getSettingsByUserId(userId).fold(
            { error ->
                when (error) {
                    is SettingsError.SettingsNotFound -> ResponseEntity.notFound().build<Settings>()
                    is SettingsError.Unknown -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.")
                }
            },
            { settings -> ResponseEntity.ok(settings) }
        )
    }

    @PostMapping("/")
    fun insertSettings(@RequestBody settings: Settings): Mono<ResponseEntity<*>> = mono {
        settingsService.insertSettings(settings).fold(
            { error -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert settings.") },
            { newSettings -> ResponseEntity.ok(newSettings) }
        )
    }

    @PostMapping("/empty/{userId}")
    fun insertEmptySettings(@PathVariable userId: Long): Mono<ResponseEntity<*>> = mono {
        settingsService.insertSettings(Settings(userId = userId)).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert empty settings.") },
            { newSettings -> ResponseEntity.ok(newSettings) }
        )
    }

    @PutMapping("/{userId}")
    fun updateSettings(@PathVariable userId: Long, @RequestBody settings: Settings): Mono<ResponseEntity<*>> = mono {
        settingsService.updateSettings(userId, settings).fold(
            { error ->
                when (error) {
                    is SettingsError.SettingsNotFound -> ResponseEntity.notFound().build<Settings>()
                    is SettingsError.Unknown -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update settings.")
                }
            },
            { updatedSettings -> ResponseEntity.ok(updatedSettings) }
        )
    }
}