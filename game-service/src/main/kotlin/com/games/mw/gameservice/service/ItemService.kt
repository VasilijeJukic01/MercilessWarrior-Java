package com.games.mw.gameservice.service

import com.games.mw.gameservice.model.Item
import com.games.mw.gameservice.repository.ItemRepository
import org.springframework.stereotype.Service

@Service
class ItemService(
    private val itemRepository: ItemRepository
) {

    fun getItemsBySettingsId(settingsId: Long): List<Item> {
        return itemRepository.findBySettingsId(settingsId)
    }

    fun getMaxId(): Long? {
        return itemRepository.findMaxId()
    }

    fun insertItem(item: Item): Item {
        return itemRepository.save(item)
    }

    fun updateItem(itemId: Long, item: Item): Item? {
        val existingItem = itemRepository.findById(itemId).orElse(null)

        return if (existingItem != null) {
            existingItem.name = item.name
            existingItem.amount = item.amount
            existingItem.equipped = item.equipped
            existingItem.settings = item.settings
            itemRepository.save(existingItem)
        } else { null }
    }

    fun deleteBySettingsId(settingsId: Long) {
        itemRepository.deleteBySettingsId(settingsId)
    }

}