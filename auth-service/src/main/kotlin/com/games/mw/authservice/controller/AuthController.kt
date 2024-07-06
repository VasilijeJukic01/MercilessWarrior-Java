package com.games.mw.authservice.controller

import com.games.mw.authservice.model.User
import com.games.mw.authservice.model.UserRole
import com.games.mw.authservice.repository.RoleRepository
import com.games.mw.authservice.repository.UserRepository
import com.games.mw.authservice.repository.UserRoleRepository
import com.games.mw.authservice.request.AuthenticationRequest
import com.games.mw.authservice.request.AuthenticationResponse
import com.games.mw.authservice.request.RegisterRequest
import com.games.mw.authservice.security.JwtService
import com.games.mw.authservice.service.LoginAttemptService
import com.games.mw.authservice.service.UserDetailsServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val loginAttemptService: LoginAttemptService,
    private val passwordEncoder: PasswordEncoder,
    private val userDetailsService: UserDetailsServiceImpl,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userRoleRepository: UserRoleRepository
) {

    @PostMapping("/register")
    fun registerUser(@RequestBody request: RegisterRequest): ResponseEntity<String> {
        if (userRepository.findByUsername(request.username).isPresent) {
            return ResponseEntity.badRequest().body("Username is already taken")
        }

        val roles = request.roles.map {
            roleRepository.findByName(it).orElseThrow { RuntimeException("Role not found: $it") }
        }.toSet()

        val newUser = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
        )

        val savedUser = userRepository.save(newUser)

        roles.forEach { role ->
            val userRole = UserRole(user = savedUser, role = role)
            userRoleRepository.save(userRole)
        }

        return ResponseEntity.ok("User registered successfully")
    }

    @PostMapping("/login")
    fun createAuthenticationToken(@RequestBody authenticationRequest: AuthenticationRequest): ResponseEntity<*> {
        val key = authenticationRequest.username

        // Brute force protection
        if (loginAttemptService.isBlocked(key)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many login attempts")
        }

        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password))
        } catch (e: Exception) {
            loginAttemptService.loginFailed(key)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed")
        }

        val userDetails: UserDetails = userDetailsService.loadUserByUsername(authenticationRequest.username)
        val jwt: String = jwtService.generateToken(userDetails)

        loginAttemptService.loginSucceeded(key)

        return ResponseEntity.ok(AuthenticationResponse(jwt))
    }
}
