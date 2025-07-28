package com.games.mw.gameservice.requests

data class ShopTransactionRequest(
    val userId: Long,
    val username: String,
    val itemId: String,
    val quantity: Int
)