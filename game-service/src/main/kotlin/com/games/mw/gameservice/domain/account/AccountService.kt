package com.games.mw.gameservice.domain.account

import arrow.core.Either
import arrow.core.raise.either
import com.games.mw.gameservice.domain.account.requests.AccountDataDTO
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.item.ItemService
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.perk.PerkService
import com.games.mw.gameservice.domain.perk.model.Perk
import com.games.mw.gameservice.domain.account.settings.SettingsService
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody

@Service
class AccountService(
    private val settingsService: SettingsService,
    private val itemService: ItemService,
    private val perkService: PerkService,
    private val webClientBuilder: WebClient.Builder,
    private val retry: Retry,
    circuitBreakerRegistry: CircuitBreakerRegistry,
    @Value("\${services.auth.url}") private val authServiceUrl: String
) {

    sealed interface AccountError {
        data class AuthServiceInteractionError(val statusCode: HttpStatus, val message: String?) : AccountError
        data class SettingsOperationFailed(val underlyingError: SettingsService.SettingsError) : AccountError
        data class ItemOperationFailed(val underlyingError: ItemService.ItemError) : AccountError
        data class PerkOperationFailed(val underlyingError: PerkService.PerkError) : AccountError
        data class UnknownSource(val throwable: Throwable) : AccountError
    }

    private val authServiceCircuitBreaker: CircuitBreaker = circuitBreakerRegistry.circuitBreaker("authServiceGame")

    suspend fun getAccountData(username: String, token: String): Either<AccountError, AccountDataDTO> = either {
        val userId = retry.executeSuspendFunction {
            authServiceCircuitBreaker.executeSuspendFunction {
                fetchUserId(username, token).bind()
            }
        }

        val settings: Settings = settingsService.getSettingsByUserId(userId).fold(
            { settingsError ->
                if (settingsError is SettingsService.SettingsError.SettingsNotFound) {
                    settingsService.insertSettings(Settings(userId = userId))
                        .mapLeft { AccountError.SettingsOperationFailed(it) }
                        .bind()
                } else {
                    raise(AccountError.SettingsOperationFailed(settingsError))
                }
            },
            { foundSettings -> foundSettings }
        )

        val settingsId = settings.id ?: raise(AccountError.UnknownSource(IllegalStateException("Settings ID is null after fetch/insert.")))

        val items: List<Item> = try { itemService.getItemsBySettingsId(settingsId) }
        catch (e: Exception) { raise(AccountError.ItemOperationFailed(ItemService.ItemError.Unknown(e))) }

        val perks: List<Perk> = try { perkService.getPerksBySettingsId(settingsId) }
        catch (e: Exception) { raise(AccountError.PerkOperationFailed(PerkService.PerkError.Unknown(e))) }

        AccountDataDTO(
            username,
            userId,
            settingsId,
            settings.spawnId,
            settings.coins,
            settings.tokens,
            settings.level,
            settings.exp,
            items.map { "${it.name},${it.amount},${it.equipped},${it.slotIndex}" },
            perks.map { it.name }
        )
    }

    @Transactional
    suspend fun updateAccountData(accountDataDTO: AccountDataDTO, token: String): Either<AccountError, Unit> = either {
        val existingSettings = settingsService.getSettingsByUserId(accountDataDTO.accountId)
            .mapLeft { AccountError.SettingsOperationFailed(it) }.bind()

        val settingsToUpdate = existingSettings.copy(
            spawnId = accountDataDTO.spawn,
            coins = accountDataDTO.coins,
            tokens = accountDataDTO.tokens,
            level = accountDataDTO.level,
            exp = accountDataDTO.exp
        )

        val settings = settingsService.updateSettings(accountDataDTO.accountId, settingsToUpdate)
            .mapLeft { AccountError.SettingsOperationFailed(it) }.bind()

        val settingsId = settings.id ?: raise(AccountError.UnknownSource(IllegalStateException("Settings ID is null after update.")))

        itemService.deleteBySettingsId(settingsId)
            .mapLeft { AccountError.ItemOperationFailed(it) }.bind()

        accountDataDTO.items.forEach {
            val itemParts = it.split(",")
            if (itemParts.size == 4) {
                val name = itemParts[0]
                val amount = itemParts[1].toInt()
                val equipped = itemParts[2].toInt()
                val slotIndex = itemParts[3].toInt()
                itemService.insertItem(Item(name = name, amount = amount, equipped = equipped, slotIndex = slotIndex, settings = settings))
                    .mapLeft { AccountError.ItemOperationFailed(it) }.bind()
            }
        }

        perkService.deleteBySettingsId(settingsId)
            .mapLeft { AccountError.PerkOperationFailed(it) }.bind()
        accountDataDTO.perks.forEach {
            perkService.insertPerk(Perk(name = it, settings = settings))
                .mapLeft { AccountError.PerkOperationFailed(it) }.bind()
        }
    }

    private suspend fun fetchUserId(username: String, token: String): Either<AccountError, Long> = either {
        val authServiceClient = webClientBuilder.baseUrl(authServiceUrl).build()
        try {
            authServiceClient.get()
                .uri("/auth/account/$username")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .awaitBody<Long>()
        } catch (e: WebClientResponseException) {
            raise(
                AccountError.AuthServiceInteractionError(
                    HttpStatus.valueOf(e.statusCode.value()),
                    e.responseBodyAsString
                )
            )
        } catch (e: Exception) {
            raise(AccountError.UnknownSource(e))
        }
    }
}