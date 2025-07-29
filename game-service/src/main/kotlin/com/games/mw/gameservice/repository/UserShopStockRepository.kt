package com.games.mw.gameservice.repository

import com.games.mw.gameservice.model.UserShopStock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserShopStockRepository : JpaRepository<UserShopStock, Long> {

    fun findBySettingsIdAndShopIdAndResetPeriod(settingsId: Long, shopId: String, resetPeriod: Long): List<UserShopStock>

    fun findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(settingsId: Long, shopId: String, itemId: String, resetPeriod: Long): Optional<UserShopStock>

}