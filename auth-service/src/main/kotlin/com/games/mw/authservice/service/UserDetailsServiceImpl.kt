package com.games.mw.authservice.service

import com.games.mw.authservice.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }

        val authorities = user.userRoles
            .map { userRole -> SimpleGrantedAuthority("ROLE_${userRole.role.name}") }
            .toSet()

        return User
            .withUsername(user.username)
            .password(user.password)
            .authorities(authorities)
            .build()
    }
}
