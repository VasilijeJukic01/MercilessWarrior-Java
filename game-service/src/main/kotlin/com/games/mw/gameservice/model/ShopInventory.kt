package com.games.mw.gameservice.model

import jakarta.persistence.*

@Entity
@Table(name = "ShopInventories", uniqueConstraints = [UniqueConstraint(columnNames = ["shopId", "itemId"])])
data class ShopInventory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val shopId: String = "",

    @ManyToOne
    @JoinColumn(name = "itemId", nullable = false)
    val item: ItemMaster = ItemMaster(),

    val stock: Int = 0,

    val cost: Int = 0
)