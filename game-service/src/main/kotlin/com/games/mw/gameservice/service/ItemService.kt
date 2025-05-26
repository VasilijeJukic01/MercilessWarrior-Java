package com.games.mw.gameservice.service

import arrow.core.Either
import arrow.core.right
import arrow.core.raise.either
import com.games.mw.gameservice.model.Item
import com.games.mw.gameservice.repository.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemService(
    private val itemRepository: ItemRepository
) {

    sealed interface ItemError {
        data object ItemNotFound : ItemError
        data class Unknown(val throwable: Throwable) : ItemError
    }

    @Transactional(readOnly = true)
    fun getItemsBySettingsId(settingsId: Long): List<Item> {
        return itemRepository.findBySettingsId(settingsId)
    }

    @Transactional
    fun insertItem(item: Item): Either<ItemError, Item> = either {
        try {
            itemRepository.save(item)
        } catch (e: Exception) {
            raise(ItemError.Unknown(e))
        }
    }

    @Transactional
    fun updateItem(itemId: Long, itemUpdate: Item): Either<ItemError, Item> = either {
        val existingItem = itemRepository.findById(itemId).orElse(null) ?: raise(ItemError.ItemNotFound)
        try {
            existingItem.name = itemUpdate.name
            existingItem.amount = itemUpdate.amount
            existingItem.equipped = itemUpdate.equipped
            existingItem.settings = itemUpdate.settings
            itemRepository.save(existingItem)
        } catch (e: Exception) {
            raise(ItemError.Unknown(e))
        }
    }

    @Transactional
    fun deleteBySettingsId(settingsId: Long): Either<ItemError, Unit> = either {
        try {
            itemRepository.deleteBySettingsId(settingsId)
            Unit.right()
        } catch (e: Exception) {
            raise(ItemError.Unknown(e))
        }
    }

}