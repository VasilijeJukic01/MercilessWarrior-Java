package com.games.mw.gameservice.domain.account

import com.games.mw.gameservice.domain.item.repository.ItemRepository
import com.games.mw.gameservice.domain.perk.repository.PerkRepository
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.item.ItemService
import com.games.mw.gameservice.domain.perk.PerkService
import com.games.mw.gameservice.security.CustomAuthenticationToken
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for checking ownership and permission of resources (settings, items, perks) for the current user.
 * It uses the security context to determine the current user's ID and verifies ownership of various entities by comparing user IDs and related settings.
 *
 * This service is designed for a reactive context.
 * It returns Mono<Boolean> and uses the mono coroutine builder to safely call suspend functions from a reactive security context.
 *
 * @property settingsService Service for accessing user settings.
 * @property itemRepository Repository for accessing item data.
 * @property perkRepository Repository for accessing perk data.
 */
@Service("permissionService")
class PermissionService(
    private val settingsService: SettingsService,
    private val itemService: ItemService,
    private val perkService: PerkService,
    private val itemRepository: ItemRepository,
    private val perkRepository: PerkRepository
) {

    private fun getCurrentUserId(authentication: Authentication): Long? {
        return (authentication as? CustomAuthenticationToken)?.userId
    }

    fun isOwnerByUserId(authentication: Authentication, userId: Long): Mono<Boolean> {
        return Mono.just(getCurrentUserId(authentication)?.let { it == userId } ?: false)
    }

    fun isOwnerOfTransactionRequest(authentication: Authentication, requestUserId: Long): Mono<Boolean> {
        return isOwnerByUserId(authentication, requestUserId)
    }

    fun isOwnerOfSettings(authentication: Authentication, settingsId: Long): Mono<Boolean> {
        val userId = getCurrentUserId(authentication) ?: return Mono.just(false)
        return mono {
            val settings = settingsService.getSettingsByUserId(userId).getOrNull()
            settings?.id == settingsId
        }.defaultIfEmpty(false)
    }

    fun isOwnerOfItem(authentication: Authentication, itemId: Long): Mono<Boolean> {
        val userId = getCurrentUserId(authentication) ?: return Mono.just(false)
        return mono {
            val item = itemService.getItemById(itemId).getOrNull() ?: return@mono false
            val settings = settingsService.getSettingsByUserId(userId).getOrNull()
            item.settings.id == settings?.id
        }.defaultIfEmpty(false)
    }

    fun isOwnerOfPerk(authentication: Authentication, perkId: Long): Mono<Boolean> {
        val userId = getCurrentUserId(authentication) ?: return Mono.just(false)
        return mono {
            val perk = perkService.getPerkById(perkId).getOrNull() ?: return@mono false
            val settings = settingsService.getSettingsByUserId(userId).getOrNull()
            perk.settings.id == settings?.id
        }.defaultIfEmpty(false)
    }

    fun isOwnerOfRequestBody(authentication: Authentication, settingsId: Long): Mono<Boolean> {
        return isOwnerOfSettings(authentication, settingsId)
    }
}