package com.games.mw.gameservice.domain.shop.transaction

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.games.mw.events.ShopTransaction
import com.games.mw.events.TransactionType
import com.games.mw.gameservice.config.game.ShopConfig
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.item.ItemService
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.model.ItemMaster
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.shop.model.ShopInventory
import com.games.mw.gameservice.domain.shop.model.UserShopStock
import com.games.mw.gameservice.domain.item.repository.ItemMasterRepository
import com.games.mw.gameservice.domain.item.repository.ItemRepository
import com.games.mw.gameservice.domain.shop.ShopService
import com.games.mw.gameservice.domain.shop.repository.ShopInventoryRepository
import com.games.mw.gameservice.domain.shop.repository.UserShopStockRepository
import com.games.mw.gameservice.domain.shop.transaction.requests.ShopTransactionRequest
import com.games.mw.gameservice.domain.shop.stream.EventProducerService
import com.games.mw.gameservice.domain.shop.transaction.requests.ShopTransactionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TransactionService(
    private val settingsService: SettingsService,
    private val itemService: ItemService,
    private val shopService: ShopService,
    private val userShopStockRepository: UserShopStockRepository,
    private val shopInventoryRepository: ShopInventoryRepository,
    private val itemMasterRepository: ItemMasterRepository,
    private val itemRepository: ItemRepository,
    private val shopConfig: ShopConfig,
    private val eventProducer: EventProducerService
) {

    sealed interface TransactionError {
        data class InsufficientFunds(val message: String = "Not enough coins.") : TransactionError
        data class ItemNotFound(val message: String = "Item not found in shop or inventory.") : TransactionError
        data class InsufficientStock(val message: String = "Not enough items in stock.") : TransactionError
        data class Unknown(val throwable: Throwable) : TransactionError
    }

    private data class TransactionContext(
        val request: ShopTransactionRequest,
        val settings: Settings,
        val shopId: String,
        val currentPeriod: Long
    )

    @Transactional
    suspend fun processBuyTransaction(request: ShopTransactionRequest): Either<TransactionError, ShopTransactionResponse> = either {
        val context = createContext(request).bind()
        executeBuy(context).bind()

        val updatedInventory = shopService.getShopInventoryForUser(context.shopId, context.settings.id!!)
        ShopTransactionResponse("Purchase successful", updatedInventory)
    }

    @Transactional
    suspend fun processSellTransaction(request: ShopTransactionRequest): Either<TransactionError, ShopTransactionResponse> = either {
        val context = createContext(request).bind()
        executeSell(context).bind()

        val updatedInventory = shopService.getShopInventoryForUser(context.shopId, context.settings.id!!)
        ShopTransactionResponse("Sale successful", updatedInventory)
    }

    // Private
    private suspend fun createContext(request: ShopTransactionRequest): Either<TransactionError, TransactionContext> = either {
        val settings = settingsService.getSettingsByUserId(request.userId).getOrNull()
            ?: raise(TransactionError.ItemNotFound("Settings not found for user."))
        TransactionContext(request, settings, "DEFAULT_SHOP", shopConfig.getCurrentResetPeriod())
    }

    private suspend fun executeBuy(context: TransactionContext): Either<TransactionError, Unit> = either {
        val masterShopItem = withContext(Dispatchers.IO) {
            shopInventoryRepository.findByShopId(context.shopId)
        }.find { it.item.itemId == context.request.itemId }
            ?: raise(TransactionError.ItemNotFound("Item not available in the shop's master list."))

        validateBuy(context, masterShopItem).bind()

        val totalCost = masterShopItem.cost * context.request.quantity
        updatePlayerCoins(context, -totalCost).bind()
        updatePlayerInventory(context, context.request.itemId, context.request.quantity).bind()
        updateUserShopStock(context, masterShopItem.item, context.request.quantity).bind()

        buildAndSendEvent(context, TransactionType.BUY, masterShopItem.cost, totalCost).bind()
    }

    private suspend fun executeSell(context: TransactionContext): Either<TransactionError, Unit> = either {
        val itemMaster = withContext(Dispatchers.IO) {
            itemMasterRepository.findById(context.request.itemId).orElse(null)
        } ?: raise(TransactionError.ItemNotFound("Item master data not found."))

        validateSell(context).bind()

        val totalGain = itemMaster.sellValue * context.request.quantity
        updatePlayerCoins(context, totalGain).bind()
        updatePlayerInventory(context, context.request.itemId, -context.request.quantity).bind()

        val masterShopItemExists = withContext(Dispatchers.IO) {
            shopInventoryRepository.findByShopId(context.shopId)
        }.any { it.item.itemId == context.request.itemId }

        if (masterShopItemExists) {
            updateUserShopStock(context, itemMaster, -context.request.quantity).bind()
        }

        buildAndSendEvent(context, TransactionType.SELL, itemMaster.sellValue, totalGain).bind()
    }

    private suspend fun validateBuy(context: TransactionContext, masterShopItem: ShopInventory): Either<TransactionError, Unit> = either {
        val userShopStock = withContext(Dispatchers.IO) {
            userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(
                context.settings.id!!, context.shopId, context.request.itemId, context.currentPeriod
            ).orElse(null)
        }

        val purchasedStock = userShopStock?.purchasedStock ?: 0
        val availableStock = masterShopItem.stock - purchasedStock
        ensure(availableStock >= context.request.quantity) { TransactionError.InsufficientStock() }

        val totalCost = masterShopItem.cost * context.request.quantity
        ensure(context.settings.coins >= totalCost) { TransactionError.InsufficientFunds() }
    }

    private suspend fun validateSell(context: TransactionContext): Either<TransactionError, Unit> = either {
        val itemToSell = withContext(Dispatchers.IO) {
            itemRepository.findBySettingsId(context.settings.id!!)
        }.find { it.name == context.request.itemId }
            ?: raise(TransactionError.ItemNotFound("Item not found in user's inventory."))
        ensure(itemToSell.amount >= context.request.quantity) { TransactionError.InsufficientStock("Not enough items to sell.") }
    }

    private suspend fun updatePlayerCoins(context: TransactionContext, coinDelta: Int): Either<TransactionError, Unit> = either {
        val updatedSettings = context.settings.copy(coins = context.settings.coins + coinDelta)
        settingsService.updateSettings(context.request.userId, updatedSettings)
            .mapLeft { TransactionError.Unknown(RuntimeException(it.toString())) }.bind()
    }

    private suspend fun updatePlayerInventory(context: TransactionContext, itemId: String, quantityDelta: Int): Either<TransactionError, Unit> = either {
        val existingItem = withContext(Dispatchers.IO) {
            itemRepository.findBySettingsId(context.settings.id!!)
        }.find { it.name == itemId }

        if (existingItem != null) {
            existingItem.amount += quantityDelta
            withContext(Dispatchers.IO) {
                if (existingItem.amount <= 0) {
                    itemRepository.delete(existingItem)
                } else {
                    itemRepository.save(existingItem)
                }
            }
        } else if (quantityDelta > 0) {
            itemService.insertItem(Item(name = itemId, amount = quantityDelta, settings = context.settings))
                .mapLeft { TransactionError.Unknown(RuntimeException(it.toString())) }.bind()
        }
    }

    private suspend fun updateUserShopStock(context: TransactionContext, itemMaster: ItemMaster, quantityDelta: Int): Either<TransactionError, Unit> = either {
        val userShopStock = withContext(Dispatchers.IO) {
            userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(
                context.settings.id!!, context.shopId, context.request.itemId, context.currentPeriod
            ).orElse(
                UserShopStock(
                    settings = context.settings,
                    shopId = context.shopId,
                    item = itemMaster,
                    purchasedStock = 0,
                    resetPeriod = context.currentPeriod
                )
            )
        }

        userShopStock.purchasedStock = maxOf(0, userShopStock.purchasedStock + quantityDelta)
        withContext(Dispatchers.IO) {
            userShopStockRepository.save(userShopStock)
        }
    }

    private fun buildAndSendEvent(context: TransactionContext, type: TransactionType, unitPrice: Int, totalPrice: Int): Either<TransactionError, Unit> = either {
        val event = ShopTransaction.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setTimestamp(Instant.now().toEpochMilli())
            .setUserId(context.request.userId)
            .setUsername(context.request.username)
            .setTransactionType(type)
            .setItemId(context.request.itemId)
            .setQuantity(context.request.quantity)
            .setUnitPrice(unitPrice)
            .setTotalPrice(totalPrice)
            .build()
        eventProducer.sendShopTransaction(event)
    }
}