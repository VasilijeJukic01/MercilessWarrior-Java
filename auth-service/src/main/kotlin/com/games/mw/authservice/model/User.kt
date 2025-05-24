package com.games.mw.authservice.model

import jakarta.persistence.*
import java.util.Objects

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
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    var userRoles: MutableSet<UserRole> = mutableSetOf()
) {
    constructor() : this(null, "", "", mutableSetOf())

    fun addUserRole(userRole: UserRole) {
        userRoles.add(userRole)
        userRole.internalSetUser(this)
    }

    fun removeUserRole(userRole: UserRole) {
        userRoles.remove(userRole)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as User
        return if (id != null) id == other.id else username == other.username
    }

    override fun hashCode(): Int {
        return if (id != null) Objects.hash(id) else Objects.hash(username)
    }

    override fun toString(): String {
        return "User(id=$id, username='$username')"
    }
}
