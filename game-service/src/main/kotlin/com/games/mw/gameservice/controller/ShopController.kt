package com.games.mw.gameservice.controller

import com.games.mw.gameservice.requests.ShopItemDTO
import com.games.mw.gameservice.requests.ShopTransactionRequest
import com.games.mw.gameservice.service.GameService
import com.games.mw.gameservice.service.ShopService
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shop")
class ShopController(
    private val gameService: GameService,
    private val shopService: ShopService
) {

    @GetMapping("/{shopId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun getShopInventory(@PathVariable shopId: String, @RequestHeader("Authorization") token: String): ResponseEntity<List<ShopItemDTO>> {
        return ResponseEntity.ok(shopService.getShopInventory(shopId))
    }

    @PostMapping("/buy")
    @PreAuthorize("@permissionService.isOwnerByUserId(#request.userId)")
    fun buyItem(@RequestBody request: ShopTransactionRequest, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return runBlocking {
            gameService.processBuyTransaction(request).fold(
                { error -> ResponseEntity.badRequest().body(error.toString()) },
                { success -> ResponseEntity.ok(success) }
            )
        }
    }

    @PostMapping("/sell")
    @PreAuthorize("@permissionService.isOwnerByUserId(#request.userId)")
    fun sellItem(@RequestBody request: ShopTransactionRequest, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return runBlocking {
            gameService.processSellTransaction(request).fold(
                { error -> ResponseEntity.badRequest().body(error.toString()) },
                { success -> ResponseEntity.ok(success) }
            )
        }
    }
}