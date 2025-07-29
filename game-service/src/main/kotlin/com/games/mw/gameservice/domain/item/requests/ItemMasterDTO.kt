package com.games.mw.gameservice.domain.item.requests

data class ItemMasterDTO(
    val itemId: String,
    val name: String,
    val description: String,
    val rarity: String,
    val imagePath: String,
    val sellValue: Int,
    val stackable: Boolean,
    val equip: EquipmentDataDTO?
)