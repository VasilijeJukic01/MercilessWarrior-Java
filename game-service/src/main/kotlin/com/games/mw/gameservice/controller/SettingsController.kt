package com.games.mw.gameservice.controller

import com.games.mw.gameservice.model.Settings
import com.games.mw.gameservice.service.SettingsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/settings")
class SettingsController (
    private val settingsService: SettingsService
) {

    @GetMapping("/{userId}")
    fun getSettingsByUserId(@PathVariable userId: Long,
                            @RequestHeader("Authorization") token: String): ResponseEntity<Settings> {
        val settings = settingsService.getSettingsByUserId(userId)

        return if (settings != null) {
            ResponseEntity.ok(settings)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/")
    fun insertSettings(@RequestBody settings: Settings,
                       @RequestHeader("Authorization") token: String): ResponseEntity<Settings> {
        val newSettings = settingsService.insertSettings(settings)

        return ResponseEntity.ok(newSettings)
    }

    @PostMapping("/empty/{userId}")
    fun insertEmptySettings(@PathVariable userId: Long,
                            @RequestHeader("Authorization") token: String): ResponseEntity<Settings> {
        val newSettings = settingsService.insertSettings(Settings(userId = userId))

        return ResponseEntity.ok(newSettings)
    }

    @PutMapping("/{userId}")
    fun updateSettings(@PathVariable userId: Long,
                       @RequestBody settings: Settings,
                       @RequestHeader("Authorization") token: String): ResponseEntity<Settings> {
        val updatedSettings = settingsService.updateSettings(userId, settings)

        return if (updatedSettings != null) {
            ResponseEntity.ok(updatedSettings)
        } else {
            ResponseEntity.notFound().build()
        }
    }

}