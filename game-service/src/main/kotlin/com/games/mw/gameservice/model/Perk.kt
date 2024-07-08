package com.games.mw.gameservice.model

import jakarta.persistence.*

@Entity
@Table(name = "Perks")
data class Perk (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @ManyToOne
    @JoinColumn(name = "settingsId", referencedColumnName = "id")
    var settings: Settings = Settings()
)