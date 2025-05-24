package com.games.mw.authservice.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "user_roles", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "role_id"])])
class UserRole(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    var role: Role
) {

    @Suppress("unused")
    constructor() : this(null, User(), Role())

    internal fun internalSetUser(user: User) {
        this.user = user
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UserRole
        return if (id != null && that.id != null) {
            id == that.id
        } else {
            (user.id != null && user.id == that.user.id && role.id != null && role.id == that.role.id) || (user.username == that.user.username && role.name == that.role.name)
        }
    }

    override fun hashCode(): Int {
        return if (id != null) {
            Objects.hash(id)
        } else {
            Objects.hash(user.id ?: user.username, role.id ?: role.name)
        }
    }

    override fun toString(): String {
        return "UserRole(id=$id, userId=${user.id ?: "lazy"}, roleId=${role.id ?: "lazy"})"
    }
}
