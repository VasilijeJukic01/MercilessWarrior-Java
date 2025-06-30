package com.games.mw.gameservice.service

import arrow.core.Either
import arrow.core.right
import arrow.core.raise.either
import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.repository.PerkRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for managing perk entities associated with user settings.
 * It provides methods to retrieve, insert, update, and delete perks by settings ID.
 *
 * @property perkRepository Repository for accessing and modifying perk data.
 */
@Service
class PerkService(
    private val perkRepository: PerkRepository
) {

    sealed interface PerkError {
        data object PerkNotFound : PerkError
        data class Unknown(val throwable: Throwable) : PerkError
    }

    /**
     * Retrieves all perks associated with the given settings ID.
     *
     * @param settingsId The ID of the settings entity.
     * @return List of [Perk]s for the specified settings.
     */
    @Transactional(readOnly = true)
    fun getPerksBySettingsId(settingsId: Long): List<Perk> {
        return perkRepository.findBySettingsId(settingsId)
    }

    /**
     * Inserts a new perk into the repository.
     *
     * @param perk The [Perk] to insert.
     * @return [Either] containing the inserted [Perk] or a [PerkError] on failure.
     */
    @Transactional
    fun insertPerk(perk: Perk): Either<PerkError, Perk> = either {
        try {
            perkRepository.save(perk)
        } catch (e: Exception) {
            raise(PerkError.Unknown(e))
        }
    }

    /**
     * Updates an existing perk by its ID.
     *
     * @param perkId The ID of the perk to update.
     * @param perkUpdate The new perk data.
     * @return [Either] containing the updated [Perk] or a [PerkError] on failure.
     */
    @Transactional
    fun updatePerk(perkId: Long, perkUpdate: Perk): Either<PerkError, Perk> = either {
        val existingPerk = perkRepository.findById(perkId).orElse(null) ?: raise(PerkError.PerkNotFound)
        try {
            existingPerk.name = perkUpdate.name
            existingPerk.settings = perkUpdate.settings
            perkRepository.save(existingPerk)
        } catch (e: Exception) {
            raise(PerkError.Unknown(e))
        }
    }

    /**
     * Deletes all perks associated with the given settings ID.
     *
     * @param settingsId The ID of the settings entity.
     * @return [Either] containing [Unit] on success or a [PerkError] on failure.
     */
    @Transactional
    fun deleteBySettingsId(settingsId: Long): Either<PerkError, Unit> = either {
        try {
            perkRepository.deleteBySettingsId(settingsId)
            Unit.right()
        } catch (e: Exception) {
            raise(PerkError.Unknown(e))
        }
    }
}