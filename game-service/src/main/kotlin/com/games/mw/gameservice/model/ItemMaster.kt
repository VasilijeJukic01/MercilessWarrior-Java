package com.games.mw.gameservice.model

import com.games.mw.gameservice.model.converter.EquipmentDataConverter
import com.games.mw.gameservice.requests.EquipmentDataDTO
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "ItemsMaster")
data class ItemMaster(
    @Id
    val itemId: String = "",

    @Column(nullable = false)
    val name: String = "",

    @Column(length = 1024)
    val description: String = "",

    @Column(nullable = false)
    val rarity: String = "",

    val imagePath: String = "",

    val sellValue: Int = 0,

    val stackable: Boolean = false,

    @Column(columnDefinition = "jsonb")
    @Convert(converter = EquipmentDataConverter::class)
    @JdbcTypeCode(SqlTypes.JSON)
    val equip: EquipmentDataDTO? = null
)