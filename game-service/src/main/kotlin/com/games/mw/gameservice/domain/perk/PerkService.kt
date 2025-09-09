package com.games.mw.gameservice.domain.perk

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.games.mw.gameservice.domain.perk.model.Perk
import com.games.mw.gameservice.domain.perk.repository.PerkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    @Transactional(readOnly = true)
    suspend fun getPerkById(perkId: Long): Either<PerkError, Perk> = either {
        val perk = withContext(Dispatchers.IO) {
            perkRepository.findById(perkId).orElse(null)
        }
        ensureNotNull(perk) { PerkError.PerkNotFound }
    }

    /**
     * Retrieves all perks associated with the given settings ID.
     *
     * @param settingsId The ID of the settings entity.
     * @return List of [Perk]s for the specified settings.
     */
    @Transactional(readOnly = true)
    suspend fun getPerksBySettingsId(settingsId: Long): List<Perk> {
        return withContext(Dispatchers.IO) {
            perkRepository.findBySettingsId(settingsId)
        }
    }

    /**
     * Inserts a new perk into the repository.
     *
     * @param perk The [Perk] to insert.
     * @return [Either] containing the inserted [Perk] or a [PerkError] on failure.
     */
    @Transactional
    suspend fun insertPerk(perk: Perk): Either<PerkError, Perk> = either {
        try {
            withContext(Dispatchers.IO) {
                perkRepository.save(perk)
            }
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
    suspend fun updatePerk(perkId: Long, perkUpdate: Perk): Either<PerkError, Perk> = either {
        val existingPerk = withContext(Dispatchers.IO) {
            perkRepository.findById(perkId).orElse(null)
        } ?: raise(PerkError.PerkNotFound)

        try {
            existingPerk.name = perkUpdate.name
            existingPerk.settings = perkUpdate.settings
            withContext(Dispatchers.IO) {
                perkRepository.save(existingPerk)
            }
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
    suspend fun deleteBySettingsId(settingsId: Long): Either<PerkError, Unit> = either {
        try {
            withContext(Dispatchers.IO) {
                val perksToDelete = perkRepository.findBySettingsId(settingsId)
                perkRepository.deleteAll(perksToDelete)
            }
        } catch (e: Exception) {
            raise(PerkError.Unknown(e))
        }
    }
}