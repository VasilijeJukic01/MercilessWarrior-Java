package com.games.mw.gameservice.service

import com.games.mw.gameservice.config.ShopConfig
import com.games.mw.gameservice.model.ItemMaster
import com.games.mw.gameservice.repository.ItemMasterRepository
import com.games.mw.gameservice.repository.ShopInventoryRepository
import com.games.mw.gameservice.repository.UserShopStockRepository
import com.games.mw.gameservice.requests.ItemMasterDTO
import com.games.mw.gameservice.requests.ShopItemDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShopService(
    private val itemMasterRepository: ItemMasterRepository,
    private val shopInventoryRepository: ShopInventoryRepository,
    private val userShopStockRepository: UserShopStockRepository,
    private val shopConfig: ShopConfig
) {

    @Transactional(readOnly = true)
    fun getAllMasterItems(): List<ItemMasterDTO> {
        return itemMasterRepository.findAll().map(this::toItemMasterDTO)
    }

    @Transactional(readOnly = true)
    fun getShopInventory(shopId: String): List<ShopItemDTO> {
        return shopInventoryRepository.findByShopId(shopId).map {
            ShopItemDTO(
                itemId = it.item.itemId,
                stock = it.stock,
                cost = it.cost
            )
        }
    }

    @Transactional(readOnly = true)
    fun getShopInventoryForUser(shopId: String, settingsId: Long): List<ShopItemDTO> {
        val currentPeriod = shopConfig.getCurrentResetPeriod()
        val masterInventory = shopInventoryRepository.findByShopId(shopId)

        val userPurchases = userShopStockRepository.findBySettingsIdAndShopIdAndResetPeriod(settingsId, shopId, currentPeriod)
            .associateBy { it.item.itemId }

        return masterInventory.map { masterItem ->
            val purchased = userPurchases[masterItem.item.itemId]?.purchasedStock ?: 0
            ShopItemDTO(
                itemId = masterItem.item.itemId,
                stock = masterItem.stock - purchased,
                cost = masterItem.cost
            )
        }.filter { it.stock > 0 }
    }

    private fun toItemMasterDTO(item: ItemMaster): ItemMasterDTO {
        return ItemMasterDTO(
            itemId = item.itemId,
            name = item.name,
            description = item.description,
            rarity = item.rarity,
            imagePath = item.imagePath,
            sellValue = item.sellValue,
            stackable = item.stackable,
            equip = item.equip
        )
    }

}