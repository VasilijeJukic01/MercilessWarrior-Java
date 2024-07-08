package com.games.mw.gameservice.controller

import com.games.mw.gameservice.model.Item
import com.games.mw.gameservice.service.ItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService
) {

    @GetMapping("/settings/{settingsId}")
    fun getItemsBySettingsId(@PathVariable settingsId: Long,
                             @RequestHeader("Authorization") token: String): ResponseEntity<List<Item>> {
        val items = itemService.getItemsBySettingsId(settingsId)

        return ResponseEntity.ok(items)
    }

    @PostMapping("/")
    fun insertItem(@RequestBody item: Item,
                   @RequestHeader("Authorization") token: String): ResponseEntity<Item> {
        val newItem = itemService.insertItem(item)

        return ResponseEntity.ok(newItem)
    }

    @PutMapping("/{itemId}")
    fun updateItem(@PathVariable itemId: Long,
                   @RequestBody item: Item,
                   @RequestHeader("Authorization") token: String): ResponseEntity<Item> {
        val updatedItem = itemService.updateItem(itemId, item)

        return if (updatedItem != null) {
            ResponseEntity.ok(updatedItem)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/settings/{settingsId}")
    fun deleteBySettingsId(@PathVariable settingsId: Long,
                           @RequestHeader("Authorization") token: String): ResponseEntity<Void> {
        itemService.deleteBySettingsId(settingsId)

        return ResponseEntity.ok().build()
    }

}