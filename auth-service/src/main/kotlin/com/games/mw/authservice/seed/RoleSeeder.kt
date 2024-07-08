package com.games.mw.authservice.seed

import com.games.mw.authservice.model.Role
import com.games.mw.authservice.repository.RoleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class RoleSeeder(
    private val roleRepository: RoleRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        seedRoles()
    }

    private fun seedRoles() {
        if (roleRepository.count() == 0L) {
            val roles = listOf(
                Role(name = "ADMIN"),
                Role(name = "USER")
            )
            roleRepository.saveAll(roles)
        }
    }
}