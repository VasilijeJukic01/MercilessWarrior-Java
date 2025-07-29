package com.games.mw.gameservice.domain.item.requests

data class EquipmentDataDTO(
    val canEquip: Boolean,
    val slot: String,
    val bonuses: Map<String, Double>?
)