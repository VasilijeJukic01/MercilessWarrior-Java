package com.games.mw.gameservice.domain.perk.model

import com.games.mw.gameservice.domain.account.settings.model.Settings
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