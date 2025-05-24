package com.games.mw.authservice.service

import arrow.core.*
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.games.mw.authservice.model.Role
import com.games.mw.authservice.model.User
import com.games.mw.authservice.model.UserRole
import com.games.mw.authservice.repository.RoleRepository
import com.games.mw.authservice.repository.UserRepository
import com.games.mw.authservice.request.AuthenticationRequest
import com.games.mw.authservice.request.AuthenticationResponse
import com.games.mw.authservice.request.RegistrationRequest
import com.games.mw.authservice.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
    private val userDetailsService: UserDetailsServiceImpl,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val loginAttemptService: LoginAttemptService
) {

    sealed interface RegistrationError {
        data object UsernameTaken : RegistrationError
        data class RoleNotFound(val roleName: String) : RegistrationError
        data class Unknown(val throwable: Throwable) : RegistrationError
    }

    sealed interface LoginError {
        data object TooManyAttempts : LoginError
        data object InvalidCredentials : LoginError
        data class Unknown(val throwable: Throwable) : LoginError
    }

    sealed interface UserAccessError {
        data object UserNotFound : UserAccessError
        data object UserIdNotAvailable : UserAccessError
    }

    @Transactional
    suspend fun registerUser(request: RegistrationRequest): Either<RegistrationError, User> = either {
        ensure(userRepository.findByUsername(request.username).isEmpty) {
            RegistrationError.UsernameTaken
        }

        val roles: Set<Role> = request.roles.traverse { roleName ->
            roleRepository.findByName(roleName)
                .map<Either<RegistrationError, Role>> { role -> role.right() }
                .orElseGet { RegistrationError.RoleNotFound(roleName).left() }
        }.map { it.toSet() }
            .bind()

        val newUser = User(
            username = request.username,
            password = passwordEncoder.encode(request.password)
        )

        // Add roles to the user
        roles.forEach { role ->
            val userRole = UserRole(user = newUser, role = role)
            newUser.addUserRole(userRole)
        }

        try {
            userRepository.save(newUser)
        } catch (e: Exception) {
            raise(RegistrationError.Unknown(e))
        }
    }

    fun loginUser(authenticationRequest: AuthenticationRequest): Either<LoginError, AuthenticationResponse> {
        val key = authenticationRequest.username
        if (loginAttemptService.isBlocked(key)) {
            return LoginError.TooManyAttempts.left()
        }

        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
            )
        } catch (e: AuthenticationException) {
            loginAttemptService.loginFailed(key)
            return LoginError.InvalidCredentials.left()
        } catch (e: Exception) {
            loginAttemptService.loginFailed(key)
            return LoginError.Unknown(e).left()
        }

        return try {
            val userDetails: UserDetails = userDetailsService.loadUserByUsername(authenticationRequest.username)
            val jwt: String = jwtService.generateToken(userDetails)
            loginAttemptService.loginSucceeded(key)
            AuthenticationResponse(jwt).right()
        } catch (e: Exception) {
            LoginError.Unknown(e).left()
        }
    }

    fun getUserIdByName(username: String): Either<UserAccessError, Long> {
        return userRepository.findByUsername(username)
            .map { user -> user.id?.right() ?: UserAccessError.UserIdNotAvailable.left() }
            .orElseGet { UserAccessError.UserIdNotAvailable.left() }
    }

    @Transactional(readOnly = true)
    fun getAllUsernames(): List<String> {
        return userRepository.findAll().map { it.username }
    }

}