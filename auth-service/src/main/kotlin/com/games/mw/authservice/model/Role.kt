package com.games.mw.authservice.model

import jakarta.persistence.*

@Entity
@Table(name = "Roles")
data class Role(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val name: String = "",

    @OneToMany(
        mappedBy = "role",
        cascade = [CascadeType.ALL],
        fetch = FetchType.EAGER
    )
    var userRoles: Set<UserRole> = emptySet()
)