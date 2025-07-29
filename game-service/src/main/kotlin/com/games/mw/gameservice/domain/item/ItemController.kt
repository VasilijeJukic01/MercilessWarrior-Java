package com.games.mw.gameservice.domain.item

import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.requests.ItemMasterDTO
import com.games.mw.gameservice.domain.item.ItemService.ItemError
import com.games.mw.gameservice.domain.shop.ShopService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService,
    private val shopService: ShopService
) {

    @GetMapping("/master")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun getMasterItemList(@RequestHeader("Authorization") token: String): ResponseEntity<List<ItemMasterDTO>> {
        return ResponseEntity.ok(shopService.getAllMasterItems())
    }

    @GetMapping("/settings/{settingsId}")
    @PreAuthorize("@permissionService.isOwnerOfSettings(#settingsId) or hasRole('ADMIN')")
    fun getItemsBySettingsId(@PathVariable settingsId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<List<Item>> {
        val items = itemService.getItemsBySettingsId(settingsId)
        return ResponseEntity.ok(items)
    }

    @PostMapping("/")
    @PreAuthorize("@permissionService.isOwnerOfRequestBody(#item.settings.id) or hasRole('ADMIN')")
    fun insertItem(@RequestBody item: Item, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return itemService.insertItem(item).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert item.") },
            { newItem -> ResponseEntity.ok(newItem) }
        )
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("@permissionService.isOwnerOfItem(#itemId) or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteBySettingsId(@PathVariable settingsId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<Void> {
        itemService.deleteBySettingsId(settingsId)
        return ResponseEntity.ok().build()
    }

}