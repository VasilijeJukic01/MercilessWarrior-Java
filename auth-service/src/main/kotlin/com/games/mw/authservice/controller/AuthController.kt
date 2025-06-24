package com.games.mw.authservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.games.mw.authservice.request.*
import com.games.mw.authservice.service.AuthService
import kotlinx.coroutines.runBlocking

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun registerUser(@RequestBody request: RegistrationRequest): ResponseEntity<*> {
        return runBlocking {
            authService.registerUser(request).fold(
                { error ->
                    when (error) {
                        is AuthService.RegistrationError.UsernameTaken ->
                            ResponseEntity.badRequest().body("Username is already taken.")
                        is AuthService.RegistrationError.RoleNotFound ->
                            ResponseEntity.badRequest().body("Role not found: ${error.roleName}.")
                        is AuthService.RegistrationError.Unknown -> {
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during registration.")
                        }
                    }
                },
                { user -> ResponseEntity.ok(user.id) }
            )
        }
    }

    @PostMapping("/login")
    fun createAuthenticationToken(@RequestBody authenticationRequest: AuthenticationRequest): ResponseEntity<*> {
        return authService.loginUser(authenticationRequest).fold(
            { error ->
                when (error) {
                    is AuthService.LoginError.TooManyAttempts ->
                        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many login attempts.")
                    is AuthService.LoginError.InvalidCredentials ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: Invalid credentials.")
                    is AuthService.LoginError.Unknown -> {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during login.")
                    }
                }
            },
            { authenticationResponse -> ResponseEntity.ok(authenticationResponse) }
        )
    }

    @GetMapping("/account/{username}")
    fun getUserIdByName(@PathVariable username: String, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return authService.getUserIdByName(username).fold(
            { error ->
                when (error) {
                    is AuthService.UserAccessError.UserNotFound ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.")
                    is AuthService.UserAccessError.UserIdNotAvailable ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User ID not available.")
                }
            },
            { userId -> ResponseEntity.ok(userId) }
        )
    }

    @GetMapping("/usernames")
    fun getAllUsernames(@RequestHeader("Authorization") token: String): ResponseEntity<List<String>> {
        return ResponseEntity.ok(authService.getAllUsernames())
    }

    @GetMapping("/validate-token")
    fun validateToken(@RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return authService.validateToken(token).fold(
            { error ->
                when (error) {
                    is AuthService.TokenError.InvalidToken ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token")
                    is AuthService.TokenError.ExpiredToken ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired")
                    is AuthService.TokenError.Unknown ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token validation failed: ${error.throwable.message}")
                }
            },
            { userInfo -> ResponseEntity.ok(userInfo) }
        )
    }

    @GetMapping("/is-admin")
    fun isAdmin(@RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return authService.isAdmin(token).fold(
            { error ->
                when (error) {
                    is AuthService.TokenError.InvalidToken ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token")
                    is AuthService.TokenError.ExpiredToken ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired")
                    is AuthService.TokenError.Unknown ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Admin check failed: ${error.throwable.message}")
                }
            },
            { isAdmin -> ResponseEntity.ok(isAdmin) }
        )
    }
}
