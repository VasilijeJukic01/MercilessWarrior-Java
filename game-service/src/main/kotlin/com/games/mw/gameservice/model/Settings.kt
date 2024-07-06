package com.games.mw.gameservice.model

import jakarta.persistence.*

@Entity
@Table(name = "Settings")
data class Settings (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val userId: Long = 0,

    var spawnId: Long = 0,

    var coins: Long = 0,

    var tokens: Long = 0,

    var exp: Long = 0,

    var level: Long = 1,

    var playtime: Long = 0,
)