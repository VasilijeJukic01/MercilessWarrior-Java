package com.games.mw.gameservice.domain.shop.transaction.requests

import com.games.mw.gameservice.domain.shop.requests.ShopItemDTO

data class ShopTransactionResponse(
    val message: String,
    val updatedShopInventory: List<ShopItemDTO>
)