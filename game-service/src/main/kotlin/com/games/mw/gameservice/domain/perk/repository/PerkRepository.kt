package com.games.mw.gameservice.domain.perk.repository

import com.games.mw.gameservice.domain.perk.model.Perk
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PerkRepository : JpaRepository<Perk, Long> {

    fun findBySettingsId(settingsId: Long): List<Perk>

    fun deleteBySettingsId(settingsId: Long)

}