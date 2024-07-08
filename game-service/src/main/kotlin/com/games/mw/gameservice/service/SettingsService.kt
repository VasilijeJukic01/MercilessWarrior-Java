package com.games.mw.gameservice.service

import com.games.mw.gameservice.model.Settings
import com.games.mw.gameservice.repository.SettingsRepository
import org.springframework.stereotype.Service

@Service
class SettingsService(
    private val settingsRepository: SettingsRepository
) {

    fun getSettingsByUserId(userId: Long): Settings? {
        return settingsRepository.findByUserId(userId)
    }

    fun insertSettings(settings: Settings): Settings {
        return settingsRepository.save(settings)
    }

    fun updateSettings(userId: Long, settings: Settings): Settings? {
        val existingSettings = settingsRepository.findByUserId(userId)

        return if (existingSettings != null) {
            existingSettings.spawnId = settings.spawnId
            existingSettings.coins = settings.coins
            existingSettings.tokens = settings.tokens
            existingSettings.exp = settings.exp
            existingSettings.level = settings.level
            existingSettings.playtime = settings.playtime
            settingsRepository.save(existingSettings)
        } else { null }
    }

}