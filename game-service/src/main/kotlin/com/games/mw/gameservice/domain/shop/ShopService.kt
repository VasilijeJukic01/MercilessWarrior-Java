package com.games.mw.gameservice.domain.shop

import arrow.core.Either
import arrow.core.raise.either
import com.games.mw.gameservice.config.game.ShopConfig
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.item.model.ItemMaster
import com.games.mw.gameservice.domain.item.repository.ItemMasterRepository
import com.games.mw.gameservice.domain.shop.repository.ShopInventoryRepository
import com.games.mw.gameservice.domain.shop.repository.UserShopStockRepository
import com.games.mw.gameservice.domain.item.requests.ItemMasterDTO
import com.games.mw.gameservice.domain.shop.requests.ShopItemDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShopService(
    private val itemMasterRepository: ItemMasterRepository,
    private val shopInventoryRepository: ShopInventoryRepository,
    private val userShopStockRepository: UserShopStockRepository,
    private val settingsService: SettingsService,
    private val shopConfig: ShopConfig
) {

    sealed interface ShopError {
        data class SettingsNotFound(val message: String = "Settings not found for user.") : ShopError
        data class Unknown(val throwable: Throwable) : ShopError
    }

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

    suspend fun getShopInventoryByUserId(shopId: String, userId: Long): Either<ShopError, List<ShopItemDTO>> = either {
        val settings = settingsService.getSettingsByUserId(userId).getOrNull()
            ?: raise(ShopError.SettingsNotFound())

        val settingsId = settings.id
            ?: raise(ShopError.Unknown(IllegalStateException("Settings ID not found for user.")))

        getShopInventoryForUser(shopId, settingsId)
    }

    internal fun getShopInventoryForUser(shopId: String, settingsId: Long): List<ShopItemDTO> {
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