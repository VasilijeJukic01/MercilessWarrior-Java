package com.games.mw.gameservice.service

import arrow.core.Either
import arrow.core.right
import arrow.core.raise.either
import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.repository.PerkRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PerkService(
    private val perkRepository: PerkRepository
) {

    sealed interface PerkError {
        data object PerkNotFound : PerkError
        data class Unknown(val throwable: Throwable) : PerkError
    }

    @Transactional(readOnly = true)
    fun getPerksBySettingsId(settingsId: Long): List<Perk> {
        return perkRepository.findBySettingsId(settingsId)
    }

    @Transactional
    fun insertPerk(perk: Perk): Either<PerkError, Perk> = either {
        try {
            perkRepository.save(perk)
        } catch (e: Exception) {
            raise(PerkError.Unknown(e))
        }
    }

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