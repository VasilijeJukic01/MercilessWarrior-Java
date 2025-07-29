package com.games.mw.gameservice.domain.item.repository

import com.games.mw.gameservice.domain.item.model.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long> {

    fun findBySettingsId(settingsId: Long): List<Item>

    fun deleteBySettingsId(settingsId: Long)

}