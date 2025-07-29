package com.games.mw.gameservice.domain.shop.requests

data class ShopItemDTO(
    val itemId: String,
    val stock: Int,
    val cost: Int
)