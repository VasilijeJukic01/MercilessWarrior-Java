package com.games.mw.gameservice.service

import com.games.mw.gameservice.model.ItemMaster
import com.games.mw.gameservice.repository.ItemMasterRepository
import com.games.mw.gameservice.repository.ShopInventoryRepository
import com.games.mw.gameservice.requests.ItemMasterDTO
import com.games.mw.gameservice.requests.ShopItemDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShopService(
    private val itemMasterRepository: ItemMasterRepository,
    private val shopInventoryRepository: ShopInventoryRepository
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