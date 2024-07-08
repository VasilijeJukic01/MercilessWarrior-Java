package com.games.mw.gameservice.repository

import com.games.mw.gameservice.model.Settings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SettingsRepository : JpaRepository<Settings, Long> {

    fun findByUserId(userId: Long): Settings?

}