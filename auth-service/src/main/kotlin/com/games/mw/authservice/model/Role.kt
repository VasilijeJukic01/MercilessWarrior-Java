package com.games.mw.authservice.model

import jakarta.persistence.*
import java.util.*

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
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    var userRoles: MutableSet<UserRole> = mutableSetOf()
) {
    constructor() : this(null, "", mutableSetOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Role
        return if (id != null) id == other.id else name == other.name
    }

    override fun hashCode(): Int {
        return if (id != null) Objects.hash(id) else Objects.hash(name)
    }

    override fun toString(): String {
        return "Role(id=$id, name='$name')"
    }
}