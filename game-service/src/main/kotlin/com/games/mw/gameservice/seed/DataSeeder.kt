package com.games.mw.gameservice.seed

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.model.ItemMaster
import com.games.mw.gameservice.model.ShopInventory
import com.games.mw.gameservice.repository.ItemMasterRepository
import com.games.mw.gameservice.repository.ShopInventoryRepository
import com.games.mw.gameservice.requests.EquipmentDataDTO
import com.games.mw.gameservice.requests.ShopItemDTO
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataSeeder(
    private val itemMasterRepository: ItemMasterRepository,
    private val shopInventoryRepository: ShopInventoryRepository,
    private val objectMapper: ObjectMapper
) : CommandLineRunner {

    private data class ItemSeedData(
        val name: String,
        val description: String,
        val rarity: String,
        val imagePath: String,
        val sellValue: Int,
        val stackable: Boolean,
        val equip: EquipmentDataDTO?
    )

    @Transactional
    override fun run(vararg args: String?) {
        seedItems()
        seedShopInventory()
    }

    private fun seedItems() {
        if (itemMasterRepository.count() > 0) return

        val typeRef = object : TypeReference<Map<String, ItemSeedData>>() {}
        val inputStream = DataSeeder::class.java.getResourceAsStream("/items/items.json")
        val items: Map<String, ItemSeedData> = objectMapper.readValue(inputStream, typeRef)

        val itemsToSave = items.map { (id, seedData) ->
            ItemMaster(
                itemId = id,
                name = seedData.name,
                description = seedData.description,
                rarity = seedData.rarity,
                imagePath = seedData.imagePath,
                sellValue = seedData.sellValue,
                stackable = seedData.stackable,
                equip = seedData.equip
            )
        }
        itemMasterRepository.saveAll(itemsToSave)
    }

    private fun seedShopInventory() {
        if (shopInventoryRepository.count() > 0) return

        val allItems = itemMasterRepository.findAll().associateBy { it.itemId }

        val typeRef = object : TypeReference<Map<String, List<ShopItemDTO>>>() {}
        val inputStream = DataSeeder::class.java.getResourceAsStream("/items/shop_inventory.json")
        val shops: Map<String, List<ShopItemDTO>> = objectMapper.readValue(inputStream, typeRef)

        val shopInventoriesToSave = shops.flatMap { (shopId, shopItems) ->
            shopItems.mapNotNull { shopItem ->
                allItems[shopItem.itemId]?.let { itemMaster ->
                    ShopInventory(
                        shopId = shopId,
                        item = itemMaster,
                        stock = shopItem.stock,
                        cost = shopItem.cost
                    )
                }
            }
        }
        shopInventoryRepository.saveAll(shopInventoriesToSave)
    }
}