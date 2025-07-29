package com.games.mw.gameservice.domain.shop.model

import com.games.mw.gameservice.domain.item.model.ItemMaster
import com.games.mw.gameservice.domain.account.settings.model.Settings
import jakarta.persistence.*

@Entity
@Table(name = "UserShopStock", uniqueConstraints = [UniqueConstraint(columnNames = ["settingsId", "shopId", "itemId", "resetPeriod"])])
data class UserShopStock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "settingsId", nullable = false)
    val settings: Settings = Settings(),

    @Column(nullable = false)
    val shopId: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId", referencedColumnName = "itemId", nullable = false)
    val item: ItemMaster = ItemMaster(),

    var purchasedStock: Int = 0,

    @Column(nullable = false)
    val resetPeriod: Long = 0L
)