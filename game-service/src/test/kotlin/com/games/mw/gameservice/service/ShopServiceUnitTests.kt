package com.games.mw.gameservice.service

import arrow.core.left
import arrow.core.right
import com.games.mw.gameservice.config.game.ShopConfig
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.item.model.ItemMaster
import com.games.mw.gameservice.domain.item.repository.ItemMasterRepository
import com.games.mw.gameservice.domain.shop.ShopService
import com.games.mw.gameservice.domain.shop.model.ShopInventory
import com.games.mw.gameservice.domain.shop.repository.ShopInventoryRepository
import com.games.mw.gameservice.domain.shop.repository.UserShopStockRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@Tag("unit")
@ExtendWith(MockitoExtension::class)
class ShopServiceUnitTests {

    @Mock private lateinit var itemMasterRepository: ItemMasterRepository
    @Mock private lateinit var shopInventoryRepository: ShopInventoryRepository
    @Mock private lateinit var userShopStockRepository: UserShopStockRepository
    @Mock private lateinit var settingsService: SettingsService
    @Mock private lateinit var shopConfig: ShopConfig

    @InjectMocks private lateinit var shopService: ShopService

    @Test
    fun `getShopInventoryByUserId when settings not found should return error`() = runBlocking {
        // Arrange
        val userId = 1L
        val shopId = "DEFAULT_SHOP"
        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(SettingsService.SettingsError.SettingsNotFound.left())

        // Act
        val result = shopService.getShopInventoryByUserId(shopId, userId)

        // Assert
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is ShopService.ShopError.SettingsNotFound)
    }

    @Test
    fun `getShopInventoryForUser when user has no purchases should return full stock`() { runBlocking {
        // Arrange
        val userId = 2L
        val settingsId = 20L
        val shopId = "DEFAULT_SHOP"
        val currentPeriod = 12345L

        val item1 = ItemMaster(itemId = "POTION")
        val item2 = ItemMaster(itemId = "SWORD")

        val masterInventory = listOf(
            ShopInventory(item = item1, stock = 10, cost = 5),
            ShopInventory(item = item2, stock = 1, cost = 100)
        )

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(Settings(id = settingsId, userId = userId).right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(currentPeriod)
        whenever(shopInventoryRepository.findByShopId(shopId)).thenReturn(masterInventory)
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndResetPeriod(settingsId, shopId, currentPeriod)).thenReturn(emptyList())

        // Act
        val result = shopService.getShopInventoryByUserId(shopId, userId)

        // Assert
        assertTrue(result.isRight())
        result.map { userInventory ->
            assertEquals(2, userInventory.size)
            assertEquals(10, userInventory.find { it.itemId == "POTION" }?.stock)
            assertEquals(1, userInventory.find { it.itemId == "SWORD" }?.stock)
        }
    }}

}