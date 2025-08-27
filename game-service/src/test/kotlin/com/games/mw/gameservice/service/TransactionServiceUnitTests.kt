package com.games.mw.gameservice.service

import arrow.core.right
import com.games.mw.events.ShopTransaction
import com.games.mw.gameservice.config.game.ShopConfig
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.item.ItemService
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.model.ItemMaster
import com.games.mw.gameservice.domain.item.repository.ItemMasterRepository
import com.games.mw.gameservice.domain.item.repository.ItemRepository
import com.games.mw.gameservice.domain.shop.ShopService
import com.games.mw.gameservice.domain.shop.model.ShopInventory
import com.games.mw.gameservice.domain.shop.model.UserShopStock
import com.games.mw.gameservice.domain.shop.repository.ShopInventoryRepository
import com.games.mw.gameservice.domain.shop.repository.UserShopStockRepository
import com.games.mw.gameservice.domain.shop.stream.EventProducerService
import com.games.mw.gameservice.domain.shop.transaction.TransactionService
import com.games.mw.gameservice.domain.shop.transaction.requests.ShopTransactionRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TransactionServiceUnitTests {

    @Mock private lateinit var settingsService: SettingsService
    @Mock private lateinit var itemService: ItemService
    @Mock private lateinit var shopService: ShopService
    @Mock private lateinit var userShopStockRepository: UserShopStockRepository
    @Mock private lateinit var shopInventoryRepository: ShopInventoryRepository
    @Mock private lateinit var itemMasterRepository: ItemMasterRepository
    @Mock private lateinit var itemRepository: ItemRepository
    @Mock private lateinit var shopConfig: ShopConfig
    @Mock private lateinit var eventProducer: EventProducerService

    @InjectMocks private lateinit var transactionService: TransactionService

    @Test
    fun `processBuyTransaction should succeed when user has enough coins and item is in stock`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "HEALTH_POTION"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 2)

        val settings = Settings(id = settingsId, userId = userId, coins = 100)
        val itemMaster = ItemMaster(itemId = itemId, name = "Health Potion")
        val masterShopItem = ShopInventory(id = 1L, shopId = "DEFAULT_SHOP", item = itemMaster, stock = 10, cost = 10)
        val userShopStock = UserShopStock(settings = settings, shopId = "DEFAULT_SHOP", item = itemMaster, purchasedStock = 1, resetPeriod = 1L)

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(1L)
        whenever(shopInventoryRepository.findByShopId("DEFAULT_SHOP")).thenReturn(listOf(masterShopItem))
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(any(), any(), any(), any())).thenReturn(Optional.of(userShopStock))
        whenever(settingsService.updateSettings(eq(userId), any())).thenAnswer { invocation ->
            (invocation.arguments[1] as Settings).right()
        }
        whenever(itemService.insertItem(argThat { name == itemId && amount == 2 })).thenReturn(Item().right())
        whenever(itemRepository.findBySettingsId(settingsId)).thenReturn(emptyList())

        // Act
        val result = transactionService.processBuyTransaction(request)

        // Assert
        assertTrue(result.isRight())
        argumentCaptor<Settings> {
            verify(settingsService).updateSettings(eq(userId), capture())
            assertEquals(80, firstValue.coins) // 100 - (2 * 10)
        }
        verify(itemService).insertItem(argThat { name == itemId && amount == 2 })
        argumentCaptor<UserShopStock> {
            verify(userShopStockRepository).save(capture())
            assertEquals(3, firstValue.purchasedStock)
        }
        verify(eventProducer).sendShopTransaction(any<ShopTransaction>())
    }}

    @Test
    fun `processBuyTransaction should fail for insufficient funds`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "HEALTH_POTION"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 2)

        val settings = Settings(id = settingsId, userId = userId, coins = 10) 
        val itemMaster = ItemMaster(itemId = itemId)
        val masterShopItem = ShopInventory(id = 1L, shopId = "DEFAULT_SHOP", item = itemMaster, stock = 10, cost = 10)

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(1L)
        whenever(shopInventoryRepository.findByShopId("DEFAULT_SHOP")).thenReturn(listOf(masterShopItem))
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(any(), any(), any(), any())).thenReturn(Optional.empty())

        // Act
        val result = transactionService.processBuyTransaction(request)

        // Assert
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is TransactionService.TransactionError.InsufficientFunds)
        verify(settingsService, never()).updateSettings(any(), any())
        verify(itemService, never()).insertItem(any())
        verify(userShopStockRepository, never()).save(any())
        verify(eventProducer, never()).sendShopTransaction(any())
    }}

    @Test
    fun `processBuyTransaction should fail for insufficient stock`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "HEALTH_POTION"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 5)

        val settings = Settings(id = settingsId, userId = userId, coins = 100)
        val itemMaster = ItemMaster(itemId = itemId)
        val masterShopItem = ShopInventory(id = 1L, shopId = "DEFAULT_SHOP", item = itemMaster, stock = 10, cost = 10)
        val userShopStock = UserShopStock(settings = settings, shopId = "DEFAULT_SHOP", item = itemMaster, purchasedStock = 8, resetPeriod = 1L) 

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(1L)
        whenever(shopInventoryRepository.findByShopId("DEFAULT_SHOP")).thenReturn(listOf(masterShopItem))
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(any(), any(), any(), any())).thenReturn(Optional.of(userShopStock))

        // Act
        val result = transactionService.processBuyTransaction(request)

        // Assert
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is TransactionService.TransactionError.InsufficientStock)
    }}

    @Test
    fun `processBuyTransaction should succeed when buying last item in stock`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "LEGENDARY_SWORD"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 1)

        val settings = Settings(id = settingsId, userId = userId, coins = 1000)
        val itemMaster = ItemMaster(itemId = itemId)
        val masterShopItem = ShopInventory(id = 1L, shopId = "DEFAULT_SHOP", item = itemMaster, stock = 1, cost = 500)

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(1L)
        whenever(shopInventoryRepository.findByShopId("DEFAULT_SHOP")).thenReturn(listOf(masterShopItem))
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(any(), any(), any(), any())).thenReturn(Optional.empty()) // Never bought before
        whenever(settingsService.updateSettings(any(), any())).thenReturn(settings.right())
        whenever(itemService.insertItem(any())).thenReturn(Item().right())
        whenever(itemRepository.findBySettingsId(settingsId)).thenReturn(emptyList())

        // Act
        val result = transactionService.processBuyTransaction(request)

        // Assert
        assertTrue(result.isRight())
        argumentCaptor<UserShopStock> {
            verify(userShopStockRepository).save(capture())
            assertEquals(1, firstValue.purchasedStock)
        }

        val request2 = ShopTransactionRequest(userId, "testuser", itemId, 1)
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(any(), any(), any(), any())).thenReturn(Optional.of(UserShopStock(purchasedStock = 1)))
        val result2 = transactionService.processBuyTransaction(request2)
        assertTrue(result2.isLeft())
        assertTrue(result2.leftOrNull() is TransactionService.TransactionError.InsufficientStock)
    }}

    @Test
    fun `processSellTransaction should succeed when user has the item`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "IRON_ORE"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 5)

        val settings = Settings(id = settingsId, userId = userId, coins = 100)
        val itemMaster = ItemMaster(itemId = itemId, name = "Iron Ore", sellValue = 5)
        val itemToSell = Item(id = 1L, name = itemId, amount = 10, settings = settings)

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(1L)
        whenever(itemMasterRepository.findById(itemId)).thenReturn(Optional.of(itemMaster))
        whenever(itemRepository.findBySettingsId(settingsId)).thenReturn(listOf(itemToSell))
        whenever(settingsService.updateSettings(any(), any())).thenReturn(settings.right())

        // Act
        val result = transactionService.processSellTransaction(request)

        // Assert
        assertTrue(result.isRight())
        argumentCaptor<Settings> {
            verify(settingsService).updateSettings(eq(userId), capture())
            assertEquals(125, firstValue.coins)
        }
        argumentCaptor<Item> {
            verify(itemRepository).save(capture())
            assertEquals(5, firstValue.amount)
        }
        verify(eventProducer).sendShopTransaction(any<ShopTransaction>())
    }}

    @Test
    fun `processSellTransaction should remove item if entire stack is sold`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "IRON_ORE"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 10)

        val settings = Settings(id = settingsId, userId = userId, coins = 100)
        val itemMaster = ItemMaster(itemId = itemId, name = "Iron Ore", sellValue = 5)
        val itemToSell = Item(id = 1L, name = itemId, amount = 10, settings = settings)
        val masterShopItem = ShopInventory(id = 1L, shopId = "DEFAULT_SHOP", item = itemMaster, stock = 10, cost = 20)

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(1L)
        whenever(itemMasterRepository.findById(itemId)).thenReturn(Optional.of(itemMaster))
        whenever(itemRepository.findBySettingsId(settingsId)).thenReturn(listOf(itemToSell))
        whenever(settingsService.updateSettings(any(), any())).thenReturn(settings.right())
        whenever(shopInventoryRepository.findByShopId("DEFAULT_SHOP")).thenReturn(listOf(masterShopItem))
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(any(), any(), any(), any())).thenReturn(Optional.empty())
        whenever(userShopStockRepository.save(any())).thenAnswer { it.getArgument(0) }

        // Act
        val result = transactionService.processSellTransaction(request)

        // Assert
        assertTrue(result.isRight())
        verify(itemRepository).delete(eq(itemToSell))
        verify(itemRepository, never()).save(any())
        verify(eventProducer).sendShopTransaction(any<ShopTransaction>())
    }}


    @Test
    fun `processSellTransaction should fail if user does not have enough items`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "IRON_ORE"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 15)

        val settings = Settings(id = settingsId, userId = userId, coins = 100)
        val itemMaster = ItemMaster(itemId = itemId, name = "Iron Ore", sellValue = 5)
        val itemToSell = Item(id = 1L, name = itemId, amount = 10, settings = settings)

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(itemMasterRepository.findById(itemId)).thenReturn(Optional.of(itemMaster))
        whenever(itemRepository.findBySettingsId(settingsId)).thenReturn(listOf(itemToSell))

        // Act
        val result = transactionService.processSellTransaction(request)

        // Assert
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is TransactionService.TransactionError.InsufficientStock)
        verify(settingsService, never()).updateSettings(any(), any())
        verify(itemRepository, never()).save(any())
        verify(itemRepository, never()).delete(any())
        verify(eventProducer, never()).sendShopTransaction(any())
    }}

    @Test
    fun `processSellTransaction should replenish user shop stock`() { runBlocking {
        // Arrange
        val userId = 1L
        val settingsId = 10L
        val itemId = "IRON_ORE"
        val request = ShopTransactionRequest(userId, "testuser", itemId, 2)

        val settings = Settings(id = settingsId, userId = userId, coins = 100)
        val itemMaster = ItemMaster(itemId = itemId, name = "Iron Ore", sellValue = 5)
        val itemToSell = Item(id = 1L, name = itemId, amount = 10, settings = settings)
        val masterShopItem = ShopInventory(id = 1L, shopId = "DEFAULT_SHOP", item = itemMaster, stock = 10, cost = 20)
        val userShopStock = UserShopStock(settings = settings, shopId = "DEFAULT_SHOP", item = itemMaster, purchasedStock = 5, resetPeriod = 1L)

        whenever(settingsService.getSettingsByUserId(userId)).thenReturn(settings.right())
        whenever(shopConfig.getCurrentResetPeriod()).thenReturn(1L)
        whenever(itemMasterRepository.findById(itemId)).thenReturn(Optional.of(itemMaster))
        whenever(itemRepository.findBySettingsId(settingsId)).thenReturn(listOf(itemToSell))
        whenever(settingsService.updateSettings(any(), any())).thenReturn(settings.right())
        whenever(shopInventoryRepository.findByShopId("DEFAULT_SHOP")).thenReturn(listOf(masterShopItem))
        whenever(userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(any(), any(), any(), any())).thenReturn(Optional.of(userShopStock))

        // Act
        val result = transactionService.processSellTransaction(request)

        // Assert
        assertTrue(result.isRight())
        argumentCaptor<UserShopStock> {
            verify(userShopStockRepository).save(capture())
            assertEquals(3, firstValue.purchasedStock)
        }
        verify(eventProducer).sendShopTransaction(any<ShopTransaction>())
    }}

}