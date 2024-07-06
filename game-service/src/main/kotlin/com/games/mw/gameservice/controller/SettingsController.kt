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
    fun getSettingsByUserId(@PathVariable userId: Long): ResponseEntity<Settings> {
        val settings = settingsService.getSettingsByUserId(userId)

        return if (settings != null) {
            ResponseEntity.ok(settings)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/maxId")
    fun getMaxId(): ResponseEntity<Long> {
        val maxId = settingsService.getMaxId()

        return if (maxId != null) {
            ResponseEntity.ok(maxId)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/")
    fun insertSettings(@RequestBody settings: Settings): ResponseEntity<Settings> {
        val newSettings = settingsService.insertSettings(settings)

        return ResponseEntity.ok(newSettings)
    }

    @PutMapping("/{userId}")
    fun updateSettings(@PathVariable userId: Long, @RequestBody settings: Settings): ResponseEntity<Settings> {
        val updatedSettings = settingsService.updateSettings(userId, settings)

        return if (updatedSettings != null) {
            ResponseEntity.ok(updatedSettings)
        } else {
            ResponseEntity.notFound().build()
        }
    }

}