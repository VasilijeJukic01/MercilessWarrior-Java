package com.games.mw.gameservice.model

import jakarta.persistence.*

@Entity
@Table(name = "UserShopStock", uniqueConstraints = [UniqueConstraint(columnNames = ["settingsId", "shopId", "itemId", "resetPeriod"])])
data class UserShopStock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "settingsId", nullable = false)
    val settings: Settings,

    @Column(nullable = false)
    val shopId: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId", referencedColumnName = "itemId", nullable = false)
    val item: ItemMaster,

    var purchasedStock: Int = 0,

    @Column(nullable = false)
    val resetPeriod: Long
)