package com.games.mw.gameservice.repository

import com.games.mw.gameservice.model.Settings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SettingsRepository : JpaRepository<Settings, Long> {

    fun findByUserId(userId: Long): Settings?

    @Query("SELECT MAX(id) FROM Settings")
    fun findMaxId(): Long?

}