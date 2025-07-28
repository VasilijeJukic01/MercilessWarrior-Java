package com.games.mw.gameservice.service

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.games.mw.events.ShopTransaction
import com.games.mw.events.TransactionType
import com.games.mw.gameservice.model.Item
import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.model.Settings
import com.games.mw.gameservice.requests.AccountDataDTO
import com.games.mw.gameservice.requests.ShopTransactionRequest
import com.games.mw.gameservice.service.stream.EventProducerService
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import java.time.Instant
import java.util.*

/**
 * Service for managing game account data.
 * Handles retrieval and update of user account data by interacting with the authentication service and internal services for settings, items,
 * and perks.
 *
 * @property settingsService Service for user settings management.
 * @property itemService Service for item management.
 * @property perkService Service for perk management.
 * @property webClientBuilder Builder for creating WebClient instances.
 * @property retry Retry configuration for external calls.
 * @property authServiceCircuitBreaker Circuit breaker for auth service communication.
 */
@Service
class GameService(
    private val settingsService: SettingsService,
    private val itemService: ItemService,
    private val perkService: PerkService,
    private val webClientBuilder: WebClient.Builder,
    private val retry: Retry,
    circuitBreakerRegistry: CircuitBreakerRegistry,
    private val eventProducer: EventProducerService
) {

    // Shop transactions logic will stay here for now due complexity

    sealed interface GameError {
        data class AuthServiceInteractionError(val statusCode: HttpStatus, val message: String?) : GameError
        data class SettingsOperationFailed(val underlyingError: SettingsService.SettingsError) : GameError
        data class ItemOperationFailed(val underlyingError: ItemService.ItemError) : GameError
        data class PerkOperationFailed(val underlyingError: PerkService.PerkError) : GameError
        data class UnknownSource(val throwable: Throwable) : GameError
    }

    sealed interface TransactionError {
        data class InsufficientFunds(val message: String = "Not enough coins.") : TransactionError
        data class ItemNotFound(val message: String = "Item not found in shop or inventory.") : TransactionError
        data class InsufficientStock(val message: String = "Not enough items in stock.") : TransactionError
        data class Unknown(val throwable: Throwable) : TransactionError
    }

    private val authServiceCircuitBreaker: CircuitBreaker = circuitBreakerRegistry.circuitBreaker("authServiceGame")

    /**
     * Retrieves complete account data for a user, including settings, items, and perks.
     *
     * @param username The username of the account.
     * @param token The authentication token for the request.
     * @return [Either] containing [AccountDataDTO] on success or [GameError] on failure.
     */
    suspend fun getAccountData(username: String, token: String): Either<GameError, AccountDataDTO> = either {
        val userId = retry.executeSuspendFunction {
            authServiceCircuitBreaker.executeSuspendFunction {
                fetchUserId(username, token).bind()
            }
        }

        val settings: Settings = settingsService.getSettingsByUserId(userId).fold(
            { settingsError ->
                if (settingsError is SettingsService.SettingsError.SettingsNotFound) {
                    settingsService.insertSettings(Settings(userId = userId))
                        .mapLeft { GameError.SettingsOperationFailed(it) }
                        .bind()
                } else {
                    raise(GameError.SettingsOperationFailed(settingsError))
                }
            },
            { foundSettings -> foundSettings }
        )

        val settingsId = settings.id ?: raise(GameError.UnknownSource(IllegalStateException("Settings ID is null after fetch/insert.")))

        val items: List<Item> = try { itemService.getItemsBySettingsId(settingsId) }
        catch (e: Exception) { raise(GameError.ItemOperationFailed(ItemService.ItemError.Unknown(e))) }

        val perks: List<Perk> = try { perkService.getPerksBySettingsId(settingsId) }
        catch (e: Exception) { raise(GameError.PerkOperationFailed(PerkService.PerkError.Unknown(e))) }

        AccountDataDTO(
            username,
            userId,
            settingsId,
            settings.spawnId,
            settings.coins,
            settings.tokens,
            settings.level,
            settings.exp,
            items.map { "${it.name},${it.amount},${it.equipped}" },
            perks.map { it.name }
        )
    }

    /**
     * Updates the account data for a user, including settings, items, and perks.
     *
     * @param accountDataDTO The new account data to update.
     * @param token The authentication token for the request.
     * @return [Either] containing [Unit] on success or [GameError] on failure.
     */
    @Transactional
    suspend fun updateAccountData(accountDataDTO: AccountDataDTO, token: String): Either<GameError, Unit> = either {
        val existingSettings = settingsService.getSettingsByUserId(accountDataDTO.accountId)
            .mapLeft { GameError.SettingsOperationFailed(it) }.bind()

        val settingsToUpdate = existingSettings.copy(
            spawnId = accountDataDTO.spawn,
            coins = accountDataDTO.coins,
            tokens = accountDataDTO.tokens,
            level = accountDataDTO.level,
            exp = accountDataDTO.exp
        )

        val settings = settingsService.updateSettings(accountDataDTO.accountId, settingsToUpdate)
            .mapLeft { GameError.SettingsOperationFailed(it) }.bind()

        val settingsId = settings.id ?: raise(GameError.UnknownSource(IllegalStateException("Settings ID is null after update.")))

        itemService.deleteBySettingsId(settingsId)
            .mapLeft { GameError.ItemOperationFailed(it) }.bind()

        accountDataDTO.items.forEach {
            val itemParts = it.split(",")
            val name = itemParts[0]
            val amount = itemParts[1].toInt()
            val equipped = itemParts[2].toInt()
            itemService.insertItem(Item(name = name, amount = amount, equipped = equipped, settings = settings))
                .mapLeft { GameError.ItemOperationFailed(it) }.bind()
        }

        perkService.deleteBySettingsId(settingsId)
            .mapLeft { GameError.PerkOperationFailed(it) }.bind()
        accountDataDTO.perks.forEach {
            perkService.insertPerk(Perk(name = it, settings = settings))
                .mapLeft { GameError.PerkOperationFailed(it) }.bind()
        }
    }

    @Transactional
    suspend fun processBuyTransaction(request: ShopTransactionRequest): Either<TransactionError, String> = either {
        // TODO: Replace with actual item price logic, e.g., fetching from a database or configuration
        val itemPrice = 10

        val settings = settingsService.getSettingsByUserId(request.userId).getOrNull()
            ?: raise(TransactionError.ItemNotFound("Settings not found for user."))

        val totalCost = itemPrice * request.quantity
        if (settings.coins < totalCost) {
            raise(TransactionError.InsufficientFunds())
        }

        settings.coins -= totalCost
        settingsService.updateSettings(request.userId, settings)
            .mapLeft { TransactionError.Unknown(RuntimeException(it.toString())) }
            .bind()

        // TODO: Replace with actual item existence check and stock management
        itemService.insertItem(Item(name = request.itemId, amount = request.quantity, settings = settings))
            .mapLeft { TransactionError.Unknown(RuntimeException(it.toString())) }
            .bind()

        val event = ShopTransaction.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setTimestamp(Instant.now().toEpochMilli())
            .setUserId(request.userId)
            .setUsername(request.username)
            .setTransactionType(TransactionType.BUY)
            .setItemId(request.itemId)
            .setQuantity(request.quantity)
            .setUnitPrice(itemPrice)
            .setTotalPrice(totalCost)
            .build()
        eventProducer.sendShopTransaction(event)

        "Purchase successful".right().bind()
    }

    @Transactional
    suspend fun processSellTransaction(request: ShopTransactionRequest): Either<TransactionError, String> = either {
        // TODO: Replace with actual item sell value logic, e.g., fetching from a database or configuration
        val itemSellValue = 5

        val settings = settingsService.getSettingsByUserId(request.userId).getOrNull()
            ?: raise(TransactionError.ItemNotFound("Settings not found for user."))

        val totalGain = itemSellValue * request.quantity
        settings.coins += totalGain
        settingsService.updateSettings(request.userId, settings)
            .mapLeft { TransactionError.Unknown(RuntimeException(it.toString())) }
            .bind()

        // TODO: Replace with actual item existence check and stock management

        val event = ShopTransaction.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setTimestamp(Instant.now().toEpochMilli())
            .setUserId(request.userId)
            .setUsername(request.username)
            .setTransactionType(TransactionType.SELL)
            .setItemId(request.itemId)
            .setQuantity(request.quantity)
            .setUnitPrice(itemSellValue)
            .setTotalPrice(totalGain)
            .build()
        eventProducer.sendShopTransaction(event)

        "Sale successful".right().bind()
    }

    // Private
    private suspend fun fetchUserId(username: String, token: String): Either<GameError, Long> = either {
        val authServiceClient = webClientBuilder.baseUrl("http://auth-service:8081").build()
        try {
            authServiceClient.get()
                .uri("/auth/account/$username")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .awaitBody<Long>()
        } catch (e: WebClientResponseException) {
            raise(GameError.AuthServiceInteractionError(HttpStatus.valueOf(e.statusCode.value()), e.responseBodyAsString))
        } catch (e: Exception) {
            raise(GameError.UnknownSource(e))
        }
    }

}