package com.games.mw.gameservice.repository

import com.games.mw.gameservice.model.ShopInventory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShopInventoryRepository : JpaRepository<ShopInventory, Long> {

    fun findByShopId(shopId: String): List<ShopInventory>

}