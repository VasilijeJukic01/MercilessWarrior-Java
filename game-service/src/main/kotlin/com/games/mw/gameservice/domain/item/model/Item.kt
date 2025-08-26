package com.games.mw.gameservice.domain.item.model

import com.games.mw.gameservice.domain.account.settings.model.Settings
import jakarta.persistence.*

@Entity
@Table(name = "Items")
data class Item (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    var amount: Int = 0,

    @Column(nullable = false)
    var equipped: Int = 0,

    @Column(nullable = false)
    var slotIndex: Int = 0,

    @ManyToOne
    @JoinColumn(name = "settingsId", referencedColumnName = "id")
    var settings: Settings = Settings()
)