package com.games.mw.gameservice.service

import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.repository.PerkRepository
import org.springframework.stereotype.Service

@Service
class PerkService(
    private val perkRepository: PerkRepository
) {

    fun getPerksBySettingsId(settingsId: Long): List<Perk> {
        return perkRepository.findBySettingsId(settingsId)
    }

    fun insertPerk(perk: Perk): Perk {
        return perkRepository.save(perk)
    }

    fun updatePerk(perkId: Long, perk: Perk): Perk? {
        val existingPerk = perkRepository.findById(perkId).orElse(null)

        return if (existingPerk != null) {
            existingPerk.name = perk.name
            existingPerk.settings = perk.settings
            perkRepository.save(existingPerk)
        } else { null }
    }

    fun deleteBySettingsId(settingsId: Long) {
        perkRepository.deleteBySettingsId(settingsId)
    }

}