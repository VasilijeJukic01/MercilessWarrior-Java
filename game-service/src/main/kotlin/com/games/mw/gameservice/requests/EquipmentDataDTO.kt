package com.games.mw.gameservice.requests

data class EquipmentDataDTO(
    val canEquip: Boolean,
    val slot: String,
    val bonuses: Map<String, Double>?
)