package com.games.mw.authservice.service

import com.games.mw.authservice.repository.UserRepository
import com.games.mw.authservice.repository.UserRoleRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }

        val userRoles = userRoleRepository.findAllByUser(user)
        val roles = userRoles.map { it.role.name }.toTypedArray()

        return User
            .withUsername(user.username)
            .password(user.password)
            .roles(*roles)
            .build()
    }
}
