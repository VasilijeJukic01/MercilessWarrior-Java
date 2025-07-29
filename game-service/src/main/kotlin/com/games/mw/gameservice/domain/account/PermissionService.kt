package com.games.mw.gameservice.domain.account

import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.perk.model.Perk
import com.games.mw.gameservice.domain.item.repository.ItemRepository
import com.games.mw.gameservice.domain.perk.repository.PerkRepository
import com.games.mw.gameservice.security.CustomAuthenticationToken
import com.games.mw.gameservice.domain.account.settings.SettingsService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

/**
 * Service for checking ownership and permission of resources (settings, items, perks) for the current user.
 * It uses the security context to determine the current user's ID and verifies ownership of various entities by comparing user IDs and related settings.
 *
 * @property settingsService Service for accessing user settings.
 * @property itemRepository Repository for accessing item data.
 * @property perkRepository Repository for accessing perk data.
 */
@Service("permissionService")
class PermissionService(
    private val settingsService: SettingsService,
    private val itemRepository: ItemRepository,
    private val perkRepository: PerkRepository
) {

    private fun getCurrentUserId(): Long? {
        val authentication = SecurityContextHolder.getContext().authentication
        return (authentication as? CustomAuthenticationToken)?.userId
    }

    fun isOwnerByUserId(userId: Long): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return currentUserId == userId
    }

    fun isOwnerOfSettings(settingsId: Long): Boolean {
        val userId = getCurrentUserId() ?: return false
        val settings = settingsService.getSettingsByUserId(userId).getOrNull()
        return settings?.id == settingsId
    }

    fun isOwnerOfItem(itemId: Long): Boolean {
        val userId = getCurrentUserId() ?: return false
        val item: Item = itemRepository.findById(itemId).orElse(null) ?: return false
        val settings = settingsService.getSettingsByUserId(userId).getOrNull()
        return item.settings.id == settings?.id
    }

    fun isOwnerOfPerk(perkId: Long): Boolean {
        val userId = getCurrentUserId() ?: return false
        val perk: Perk = perkRepository.findById(perkId).orElse(null) ?: return false
        val settings = settingsService.getSettingsByUserId(userId).getOrNull()
        return perk.settings.id == settings?.id
    }

    fun isOwnerOfRequestBody(settingsId: Long): Boolean {
        return isOwnerOfSettings(settingsId)
    }
}