package com.games.mw.gameservice.domain.shop.model

import com.games.mw.gameservice.domain.item.model.ItemMaster
import jakarta.persistence.*

@Entity
@Table(name = "ShopInventories", uniqueConstraints = [UniqueConstraint(columnNames = ["shopId", "itemId"])])
data class ShopInventory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val shopId: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId", nullable = false)
    val item: ItemMaster = ItemMaster(),

    var stock: Int = 0,
    val cost: Int = 0
)