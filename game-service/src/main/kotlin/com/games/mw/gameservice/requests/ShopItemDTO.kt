package com.games.mw.gameservice.requests

data class ShopItemDTO(
    val itemId: String,
    val stock: Int,
    val cost: Int
)