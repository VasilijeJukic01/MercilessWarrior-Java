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

/**
 * Service for handling authentication, registration, and user/token management.
 *
 * @property authenticationManager Spring Security authentication manager.
 * @property passwordEncoder Password encoder for hashing user passwords.
 * @property userDetailsService Loads user details for authentication.
 * @property jwtService Service for generating and validating JWT tokens.
 * @property userRepository Repository for user data access.
 * @property roleRepository Repository for role data access.
 * @property loginAttemptService Service for tracking login attempts and blocking.
 */
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

    sealed interface TokenError {
        data object InvalidToken : TokenError
        data object ExpiredToken : TokenError
        data class Unknown(val throwable: Throwable) : TokenError
    }

    /**
     * Registers a new user with the provided roles.
     *
     * @param request Registration request containing username, password, and roles.
     * @return Either a [User] on success or a [RegistrationError] on failure.
     */
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

    /**
     * Authenticates a user and returns a JWT token if successful.
     *
     * @param authenticationRequest Contains username and password.
     * @return Either an [AuthenticationResponse] with JWT or a [LoginError].
     */
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

    /**
     * Retrieves the user ID by username.
     *
     * @param username The username to look up.
     * @return Either the user ID or a [UserAccessError].
     */
    fun getUserIdByName(username: String): Either<UserAccessError, Long> {
        return userRepository.findByUsername(username)
            .map { user -> user.id?.right() ?: UserAccessError.UserIdNotAvailable.left() }
            .orElseGet { UserAccessError.UserIdNotAvailable.left() }
    }

    /**
     * Returns a list of all usernames in the system.
     */
    @Transactional(readOnly = true)
    fun getAllUsernames(): List<String> {
        return userRepository.findAll().map { it.username }
    }

    /**
     * Validates a JWT token and extracts user information.
     *
     * @param token The JWT token to validate.
     * @return Either a map of user info or a [TokenError].
     */
    fun validateToken(token: String): Either<TokenError, Map<String, Any>> {
        return try {
            val username = jwtService.extractUsername(token)
            val userDetails = userDetailsService.loadUserByUsername(username)

            if (jwtService.validateToken(token, userDetails)) {
                val user = userRepository.findByUsername(username)
                    .orElseThrow { IllegalStateException("User not found despite valid token") }

                val roles = user.userRoles.map { it.role.name }

                mapOf("username" to username, "roles" to roles).right()
            } else {
                TokenError.ExpiredToken.left()
            }
        } catch (e: Exception) {
            TokenError.Unknown(e).left()
        }
    }

    /**
     * Checks if the user associated with the token has the ADMIN role.
     *
     * @param token The JWT token.
     * @return Either true if admin, false otherwise, or a [TokenError].
     */
    fun isAdmin(token: String): Either<TokenError, Boolean> {
        return validateToken(token).map { userInfo ->
            @Suppress("UNCHECKED_CAST")
            val roles = userInfo["roles"] as List<String>
            roles.contains("ADMIN")
        }
    }
}