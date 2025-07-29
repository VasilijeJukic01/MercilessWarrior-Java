package com.games.mw.gameservice.repository

import com.games.mw.gameservice.model.ShopInventory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ShopInventoryRepository : JpaRepository<ShopInventory, Long> {

    @Query("SELECT si FROM ShopInventory si JOIN FETCH si.item WHERE si.shopId = :shopId")
    fun findByShopId(shopId: String): List<ShopInventory>

}