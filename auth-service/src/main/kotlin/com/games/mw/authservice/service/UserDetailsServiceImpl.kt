package com.games.mw.authservice.service

import com.games.mw.authservice.repository.UserRepository
import com.games.mw.authservice.security.CustomUserDetails
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for loading user-specific data for authentication.
 * Implements [UserDetailsService] to provide user details to Spring Security.
 *
 * @property userRepository Repository for accessing user data.
 */
@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    /**
     * Loads the user by username, including their roles.
     *
     * @param username the username identifying the user whose data is required.
     * @return [UserDetails] containing user information and authorities.
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }

        val authorities = user.userRoles
            .map { userRole -> SimpleGrantedAuthority("ROLE_${userRole.role.name}") }
            .toSet()

        return CustomUserDetails(
            id = user.id!!,
            username = user.username,
            password = user.password,
            authorities = authorities
        )
    }
}
