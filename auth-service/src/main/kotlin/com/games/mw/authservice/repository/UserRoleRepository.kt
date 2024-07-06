package com.games.mw.authservice.repository

import com.games.mw.authservice.model.User
import com.games.mw.authservice.model.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRoleRepository : JpaRepository<UserRole, Long> {

    fun findAllByUser(user: User): List<UserRole>

}
