package com.games.mw.gameservice.domain.shop

import com.games.mw.gameservice.domain.shop.transaction.requests.ShopTransactionRequest
import com.games.mw.gameservice.domain.shop.transaction.TransactionService
import com.games.mw.gameservice.security.CustomAuthenticationToken
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shop")
class ShopController(
    private val transactionService: TransactionService,
    private val shopService: ShopService,
) {

    @GetMapping("/{shopId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun getShopInventory(
        @PathVariable shopId: String,
        authentication: Authentication
    ): ResponseEntity<*> {
        val authToken = authentication as? CustomAuthenticationToken
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid authentication token.")

        val userId = authToken.userId

        return runBlocking {
            shopService.getShopInventoryByUserId(shopId, userId).fold(
                { error ->
                    when (error) {
                        is ShopService.ShopError.SettingsNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.message)
                        is ShopService.ShopError.Unknown -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred while fetching shop data.")
                    }
                },
                { userShopInventory -> ResponseEntity.ok(userShopInventory) }
            )
        }
    }

    @PostMapping("/buy")
    @PreAuthorize("@permissionService.isOwnerByUserId(#request.userId)")
    fun buyItem(@RequestBody request: ShopTransactionRequest, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return runBlocking {
            transactionService.processBuyTransaction(request).fold(
                { error -> ResponseEntity.badRequest().body(error.toString()) },
                { successResponse -> ResponseEntity.ok(successResponse) }
            )
        }
    }

    @PostMapping("/sell")
    @PreAuthorize("@permissionService.isOwnerByUserId(#request.userId)")
    fun sellItem(@RequestBody request: ShopTransactionRequest, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return runBlocking {
            transactionService.processSellTransaction(request).fold(
                { error -> ResponseEntity.badRequest().body(error.toString()) },
                { successResponse -> ResponseEntity.ok(successResponse) }
            )
        }
    }
}