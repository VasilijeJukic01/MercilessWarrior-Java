package com.games.mw.gameservice.controller

import com.games.mw.gameservice.model.Settings
import com.games.mw.gameservice.service.SettingsService.SettingsError
import com.games.mw.gameservice.service.SettingsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/settings")
class SettingsController (
    private val settingsService: SettingsService
) {

    @GetMapping("/{userId}")
    fun getSettingsByUserId(@PathVariable userId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return settingsService.getSettingsByUserId(userId).fold(
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
    fun insertSettings(@RequestBody settings: Settings, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return settingsService.insertSettings(settings).fold(
            { error -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert settings.") },
            { newSettings -> ResponseEntity.ok(newSettings) }
        )
    }

    @PostMapping("/empty/{userId}")
    fun insertEmptySettings(@PathVariable userId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return settingsService.insertSettings(Settings(userId = userId)).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert empty settings.") },
            { newSettings -> ResponseEntity.ok(newSettings) }
        )
    }

    @PutMapping("/{userId}")
    fun updateSettings(@PathVariable userId: Long, @RequestBody settings: Settings, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return settingsService.updateSettings(userId, settings).fold(
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