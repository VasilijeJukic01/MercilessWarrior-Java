package com.games.mw.gameservice.domain.item

import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.requests.ItemMasterDTO
import com.games.mw.gameservice.domain.item.ItemService.ItemError
import com.games.mw.gameservice.domain.shop.ShopService
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService,
    private val shopService: ShopService
) {

    @GetMapping("/master")
    fun getMasterItemList(): Mono<ResponseEntity<List<ItemMasterDTO>>> = mono {
        ResponseEntity.ok(shopService.getAllMasterItems())
    }

    @GetMapping("/settings/{settingsId}")
    fun getItemsBySettingsId(@PathVariable settingsId: Long): Mono<ResponseEntity<List<Item>>> = mono {
        ResponseEntity.ok(itemService.getItemsBySettingsId(settingsId))
    }

    @PostMapping("/")
    fun insertItem(@RequestBody item: Item): Mono<ResponseEntity<*>> = mono {
        itemService.insertItem(item).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert item.") },
            { newItem -> ResponseEntity.ok(newItem) }
        )
    }

    @PutMapping("/{itemId}")
    fun updateItem(@PathVariable itemId: Long, @RequestBody item: Item): Mono<ResponseEntity<*>> = mono {
        itemService.updateItem(itemId, item).fold(
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
    fun deleteBySettingsId(@PathVariable settingsId: Long): Mono<ResponseEntity<*>> = mono {
        itemService.deleteBySettingsId(settingsId).fold(
            { ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete items.") },
            { ResponseEntity.ok().build<Void>() }
        )
    }
}