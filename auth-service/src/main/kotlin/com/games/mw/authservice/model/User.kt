package com.games.mw.authservice.model

import jakarta.persistence.*

@Entity
@Table(name = "Users")
data class User (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val username: String = "",

    @Column(nullable = false)
    val password: String = "",

    @OneToMany(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        fetch = FetchType.EAGER
    )
    val userRoles: Set<UserRole> = emptySet()
)
