package com.games.mw.gameservice.domain.shop

import com.games.mw.gameservice.domain.shop.requests.ShopItemDTO
import com.games.mw.gameservice.domain.shop.transaction.requests.ShopTransactionRequest
import com.games.mw.gameservice.domain.shop.transaction.TransactionService
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shop")
class ShopController(
    private val transactionService: TransactionService,
    private val shopService: ShopService
) {

    @GetMapping("/{shopId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun getShopInventory(@PathVariable shopId: String, @RequestHeader("Authorization") token: String): ResponseEntity<List<ShopItemDTO>> {
        return ResponseEntity.ok(shopService.getShopInventory(shopId))
    }

    @PostMapping("/buy")
    @PreAuthorize("@permissionService.isOwnerByUserId(#request.userId)")
    suspend fun buyItem(@RequestBody request: ShopTransactionRequest, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return transactionService.processBuyTransaction(request).fold(
            { error -> ResponseEntity.badRequest().body(error.toString()) },
            { success -> ResponseEntity.ok(success) }
        )
    }

    @PostMapping("/sell")
    @PreAuthorize("@permissionService.isOwnerByUserId(#request.userId)")
    suspend fun sellItem(@RequestBody request: ShopTransactionRequest, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return transactionService.processSellTransaction(request).fold(
            { error -> ResponseEntity.badRequest().body(error.toString()) },
            { success -> ResponseEntity.ok(success) }
        )
    }
}