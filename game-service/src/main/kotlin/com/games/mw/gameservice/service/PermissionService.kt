package com.games.mw.gameservice.service

import com.games.mw.gameservice.model.Item
import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.repository.ItemRepository
import com.games.mw.gameservice.repository.PerkRepository
import com.games.mw.gameservice.security.CustomAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

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