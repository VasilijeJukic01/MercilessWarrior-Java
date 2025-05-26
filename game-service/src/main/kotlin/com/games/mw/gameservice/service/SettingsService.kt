package com.games.mw.gameservice.service

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.games.mw.gameservice.model.Settings
import com.games.mw.gameservice.repository.SettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SettingsService(
    private val settingsRepository: SettingsRepository
) {

    sealed interface SettingsError {
        data object SettingsNotFound : SettingsError
        data class Unknown(val throwable: Throwable) : SettingsError
    }

    @Transactional(readOnly = true)
    fun getSettingsByUserId(userId: Long): Either<SettingsError, Settings> = either {
        ensureNotNull(settingsRepository.findByUserId(userId)) { SettingsError.SettingsNotFound }
    }

    @Transactional
    fun insertSettings(settings: Settings): Either<SettingsError, Settings> = either {
        try {
            settingsRepository.save(settings)
        } catch (e: Exception) {
            raise(SettingsError.Unknown(e))
        }
    }

    @Transactional
    fun updateSettings(userId: Long, settingsUpdate: Settings): Either<SettingsError, Settings> = either {
        val existingSettings = settingsRepository.findByUserId(userId) ?: raise(SettingsError.SettingsNotFound)

        try {
            existingSettings.spawnId = settingsUpdate.spawnId
            existingSettings.coins = settingsUpdate.coins
            existingSettings.tokens = settingsUpdate.tokens
            existingSettings.exp = settingsUpdate.exp
            existingSettings.level = settingsUpdate.level
            existingSettings.playtime = settingsUpdate.playtime
            settingsRepository.save(existingSettings)
        } catch (e: Exception) {
            raise(SettingsError.Unknown(e))
        }
    }

}