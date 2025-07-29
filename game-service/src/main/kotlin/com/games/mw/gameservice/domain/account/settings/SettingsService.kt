package com.games.mw.gameservice.domain.account.settings

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for managing user settings entities.
 * It provides methods to retrieve, insert, and update settings by user ID.
 *
 * @property settingsRepository Repository for accessing and modifying settings data.
 */
@Service
class SettingsService(
    private val settingsRepository: SettingsRepository
) {

    sealed interface SettingsError {
        data object SettingsNotFound : SettingsError
        data class Unknown(val throwable: Throwable) : SettingsError
    }

    /**
     * Retrieves the settings associated with the given user ID.
     *
     * @param userId The ID of the user.
     * @return [Either] containing the [Settings] on success or a [SettingsError] on failure.
     */
    @Transactional(readOnly = true)
    fun getSettingsByUserId(userId: Long): Either<SettingsError, Settings> = either {
        ensureNotNull(settingsRepository.findByUserId(userId)) { SettingsError.SettingsNotFound }
    }

    /**
     * Inserts a new settings entity into the repository.
     *
     * @param settings The [Settings] to insert.
     * @return [Either] containing the inserted [Settings] or a [SettingsError] on failure.
     */
    @Transactional
    fun insertSettings(settings: Settings): Either<SettingsError, Settings> = either {
        try {
            settingsRepository.save(settings)
        } catch (e: Exception) {
            raise(SettingsError.Unknown(e))
        }
    }

    /**
     * Updates an existing settings entity by user ID.
     *
     * @param userId The ID of the user whose settings are to be updated.
     * @param settingsUpdate The new settings data.
     * @return [Either] containing the updated [Settings] or a [SettingsError] on failure.
     */
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