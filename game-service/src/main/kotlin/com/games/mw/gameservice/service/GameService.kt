package com.games.mw.gameservice.service

import com.games.mw.gameservice.model.Item
import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.model.Settings
import com.games.mw.gameservice.requests.AccountDataDTO
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import io.github.resilience4j.reactor.retry.RetryOperator
import io.github.resilience4j.retry.Retry
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.HttpHeaders

@Service
class GameService(
    private val settingsService: SettingsService,
    private val itemService: ItemService,
    private val perkService: PerkService,
    private val webClientBuilder: WebClient.Builder,
    private val retry: Retry,
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    private val authServiceCircuitBreaker: CircuitBreaker = circuitBreakerRegistry.circuitBreaker("authService")

    fun getAccountData(username: String, token: String): AccountDataDTO {
        val authServiceClient = webClientBuilder.baseUrl("http://auth-service:8081").build()

        val userId = authServiceClient.get()
            .uri("/auth/account/$username")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .bodyToMono(Long::class.java)
            .transform(CircuitBreakerOperator.of(authServiceCircuitBreaker))
            .transformDeferred(RetryOperator.of(retry))
            .block()

        var settings = settingsService.getSettingsByUserId(userId!!)
        if (settings == null) {
            settings = settingsService.insertSettings(Settings(userId = userId))
        }

        val items = itemService.getItemsBySettingsId(settings.id!!)
        val perks = perkService.getPerksBySettingsId(settings.id!!)

        return AccountDataDTO(
            username,
            userId,
            settings.id!!,
            settings.spawnId,
            settings.coins,
            settings.tokens,
            settings.level,
            settings.exp,
            items.map { it.name + "," + it.amount + "," + it.equipped },
            perks.map { it.name }
        )
    }

    fun updateAccountData(accountDataDTO: AccountDataDTO) {
        val settings = settingsService.getSettingsByUserId(accountDataDTO.accountId)
        settings?.coins = accountDataDTO.coins
        settings?.tokens = accountDataDTO.tokens
        settings?.level = accountDataDTO.level
        settings?.exp = accountDataDTO.exp
        settingsService.updateSettings(accountDataDTO.accountId, settings!!)

        // Item handling
        val items = itemService.getItemsBySettingsId(settings.id!!)
        items.forEach { itemService.deleteBySettingsId(it.id!!) }
        accountDataDTO.items.forEach {
            val itemParts = it.split(",")
            val name = itemParts[0]
            val amount = itemParts[1].toInt()
            val equiped = itemParts[2].toInt()
            itemService.insertItem(Item(name = name, amount = amount, equipped = equiped, settings = settings))
        }

        // Perk handling
        val perks = perkService.getPerksBySettingsId(settings.id)
        perks.forEach { perkService.deleteBySettingsId(it.id!!) }
        accountDataDTO.perks.forEach {
            perkService.insertPerk(Perk(name = it, settings = settings))
        }
    }

}