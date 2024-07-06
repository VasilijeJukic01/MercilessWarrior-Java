package com.games.mw.gameservice.model

import jakarta.persistence.*

@Entity
@Table(name = "Items")
data class Item (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    var amount: Long = 0,

    var equipped: Int = 0,

    @ManyToOne
    @JoinColumn(name = "settingsId", referencedColumnName = "id")
    var settings: Settings = Settings()
)