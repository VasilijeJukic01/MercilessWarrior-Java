package com.games.mw.gameservice.domain.shop.transaction

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
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
import com.games.mw.gameservice.domain.shop.repository.ShopInventoryRepository
import com.games.mw.gameservice.domain.shop.repository.UserShopStockRepository
import com.games.mw.gameservice.domain.shop.transaction.requests.ShopTransactionRequest
import com.games.mw.gameservice.domain.shop.stream.EventProducerService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TransactionService(
    private val settingsService: SettingsService,
    private val itemService: ItemService,
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
    suspend fun processBuyTransaction(request: ShopTransactionRequest): Either<TransactionError, String> = either {
        val context = createContext(request).bind()
        executeBuy(context).bind()
        "Purchase successful".right().bind()
    }

    @Transactional
    suspend fun processSellTransaction(request: ShopTransactionRequest): Either<TransactionError, String> = either {
        val context = createContext(request).bind()
        executeSell(context).bind()
        "Sale successful".right().bind()
    }

    // Private
    private suspend fun createContext(request: ShopTransactionRequest): Either<TransactionError, TransactionContext> = either {
        val settings = settingsService.getSettingsByUserId(request.userId).getOrNull()
            ?: raise(TransactionError.ItemNotFound("Settings not found for user."))
        TransactionContext(request, settings, "DEFAULT_SHOP", shopConfig.getCurrentResetPeriod())
    }

    private suspend fun executeBuy(context: TransactionContext): Either<TransactionError, Unit> = either {
        val masterShopItem = shopInventoryRepository.findByShopId(context.shopId).find { it.item.itemId == context.request.itemId }
            ?: raise(TransactionError.ItemNotFound("Item not available in the shop's master list."))

        validateBuy(context, masterShopItem).bind()

        val totalCost = masterShopItem.cost * context.request.quantity
        updatePlayerCoins(context, -totalCost).bind()
        updatePlayerInventory(context, context.request.itemId, context.request.quantity).bind()
        updateUserShopStock(context, masterShopItem, context.request.quantity).bind()

        buildAndSendEvent(context, TransactionType.BUY, masterShopItem.cost, totalCost).bind()
    }

    private suspend fun executeSell(context: TransactionContext): Either<TransactionError, Unit> = either {
        val itemMaster = itemMasterRepository.findById(context.request.itemId).orElse(null)
            ?: raise(TransactionError.ItemNotFound("Item master data not found."))

        validateSell(context).bind()

        val totalGain = itemMaster.sellValue * context.request.quantity
        updatePlayerCoins(context, totalGain).bind()
        updatePlayerInventory(context, context.request.itemId, -context.request.quantity).bind()

        val masterShopItem = shopInventoryRepository.findByShopId(context.shopId).find { it.item.itemId == context.request.itemId }
        if (masterShopItem != null) {
            updateUserShopStock(context, itemMaster, -context.request.quantity).bind()
        }

        buildAndSendEvent(context, TransactionType.SELL, itemMaster.sellValue, totalGain).bind()
    }

    private suspend fun validateBuy(context: TransactionContext, masterShopItem: ShopInventory): Either<TransactionError, Unit> = either {
        val userShopStock = userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(context.settings.id!!, context.shopId, context.request.itemId, context.currentPeriod)
            .orElse(UserShopStock(settings = context.settings, shopId = context.shopId, item = masterShopItem.item, purchasedStock = 0, resetPeriod = context.currentPeriod))

        val availableStock = masterShopItem.stock - userShopStock.purchasedStock
        ensure(availableStock >= context.request.quantity) { TransactionError.InsufficientStock() }

        val totalCost = masterShopItem.cost * context.request.quantity
        ensure(context.settings.coins >= totalCost) { TransactionError.InsufficientFunds() }
    }

    private fun validateSell(context: TransactionContext): Either<TransactionError, Unit> = either {
        val itemToSell = itemRepository.findBySettingsId(context.settings.id!!)
            .find { it.name == context.request.itemId }
            ?: raise(TransactionError.ItemNotFound("Item not found in user's inventory."))
        ensure(itemToSell.amount >= context.request.quantity) { TransactionError.InsufficientStock("Not enough items to sell.") }
    }

    private suspend fun updatePlayerCoins(context: TransactionContext, coinDelta: Int): Either<TransactionError, Unit> = either {
        val updatedSettings = context.settings.copy(coins = context.settings.coins + coinDelta)
        settingsService.updateSettings(context.request.userId, updatedSettings)
            .mapLeft { TransactionError.Unknown(RuntimeException(it.toString())) }.bind()
    }

    private suspend fun updatePlayerInventory(context: TransactionContext, itemId: String, quantityDelta: Int): Either<TransactionError, Unit> = either {
        val existingItem = itemRepository.findBySettingsId(context.settings.id!!)
            .find { it.name == itemId }

        if (existingItem != null) {
            existingItem.amount += quantityDelta
            if (existingItem.amount <= 0) {
                itemRepository.delete(existingItem)
            } else {
                try {
                    itemRepository.save(existingItem)
                } catch (e: Exception) {
                    raise(TransactionError.Unknown(e))
                }
            }
        } else if (quantityDelta > 0) {
            itemService.insertItem(Item(name = itemId, amount = quantityDelta, settings = context.settings))
                .mapLeft { TransactionError.Unknown(RuntimeException(it.toString())) }.bind()
        }
    }

    private suspend fun updateUserShopStock(context: TransactionContext, itemMaster: Any, quantityDelta: Int): Either<TransactionError, Unit> = either {
        val master = when(itemMaster) {
            is ShopInventory -> itemMaster.item
            is ItemMaster -> itemMaster
            else -> raise(TransactionError.Unknown(IllegalArgumentException("Invalid item master type")))
        }

        val userShopStock = userShopStockRepository.findBySettingsIdAndShopIdAndItem_ItemIdAndResetPeriod(context.settings.id!!, context.shopId, context.request.itemId, context.currentPeriod)
            .orElse(UserShopStock(settings = context.settings, shopId = context.shopId, item = master, purchasedStock = 0, resetPeriod = context.currentPeriod))

        userShopStock.purchasedStock = maxOf(0, userShopStock.purchasedStock + quantityDelta)
        userShopStockRepository.save(userShopStock)
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