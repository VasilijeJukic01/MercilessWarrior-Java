package com.games.mw.gameservice.domain.item

import arrow.core.Either
import arrow.core.right
import arrow.core.raise.either
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.repository.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for managing item entities associated with user settings.
 * It provides methods to retrieve, insert, update, and delete items by settings ID.
 *
 * @property itemRepository Repository for accessing and modifying item data.
 */
@Service
class ItemService(
    private val itemRepository: ItemRepository
) {

    sealed interface ItemError {
        data object ItemNotFound : ItemError
        data class Unknown(val throwable: Throwable) : ItemError
    }

    /**
     * Retrieves all items associated with the given settings ID.
     *
     * @param settingsId The ID of the settings entity.
     * @return List of [Item]s for the specified settings.
     */
    @Transactional(readOnly = true)
    fun getItemsBySettingsId(settingsId: Long): List<Item> {
        return itemRepository.findBySettingsId(settingsId)
    }

    /**
     * Inserts a new item into the repository.
     *
     * @param item The [Item] to insert.
     * @return [Either] containing the inserted [Item] or an [ItemError] on failure.
     */
    @Transactional
    fun insertItem(item: Item): Either<ItemError, Item> = either {
        try {
            itemRepository.save(item)
        } catch (e: Exception) {
            raise(ItemError.Unknown(e))
        }
    }

    /**
     * Updates an existing item by its ID.
     *
     * @param itemId The ID of the item to update.
     * @param itemUpdate The new item data.
     * @return [Either] containing the updated [Item] or an [ItemError] on failure.
     */
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

    /**
     * Deletes all items associated with the given settings ID.
     *
     * @param settingsId The ID of the settings entity.
     * @return [Either] containing [Unit] on success or an [ItemError] on failure.
     */
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