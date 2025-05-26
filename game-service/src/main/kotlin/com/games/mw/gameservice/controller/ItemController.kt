package com.games.mw.gameservice.controller

import com.games.mw.gameservice.model.Item
import com.games.mw.gameservice.service.ItemService.ItemError
import com.games.mw.gameservice.service.ItemService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService
) {

    @GetMapping("/settings/{settingsId}")
    fun getItemsBySettingsId(@PathVariable settingsId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<List<Item>> {
        val items = itemService.getItemsBySettingsId(settingsId)
        return ResponseEntity.ok(items)
    }

    @PostMapping("/")
    fun insertItem(@RequestBody item: Item, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return itemService.insertItem(item).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert item.") },
            { newItem -> ResponseEntity.ok(newItem) }
        )
    }

    @PutMapping("/{itemId}")
    fun updateItem(@PathVariable itemId: Long, @RequestBody item: Item, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return itemService.updateItem(itemId, item).fold(
            { error ->
                when (error) {
                    is ItemError.ItemNotFound -> ResponseEntity.notFound().build<Item>()
                    is ItemError.Unknown -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update item.")
                }
            },
            { updatedItem -> ResponseEntity.ok(updatedItem) }
        )
    }

    @DeleteMapping("/settings/{settingsId}")
    fun deleteBySettingsId(@PathVariable settingsId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<Void> {
        itemService.deleteBySettingsId(settingsId)
        return ResponseEntity.ok().build()
    }

}