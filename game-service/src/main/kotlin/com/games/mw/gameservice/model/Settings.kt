package com.games.mw.gameservice.model

import jakarta.persistence.*

@Entity
@Table(name = "Settings")
data class Settings (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val userId: Long = 0,

    var spawnId: Int = 0,

    var coins: Int = 0,

    var tokens: Int = 0,

    var exp: Int = 0,

    var level: Int = 1,

    var playtime: Int = 0,
)