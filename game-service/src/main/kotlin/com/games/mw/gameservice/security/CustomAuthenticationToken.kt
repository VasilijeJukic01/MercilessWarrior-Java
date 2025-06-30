package com.games.mw.gameservice.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * Custom authentication token that includes a user ID in addition to the standard principal and authorities.
 * Used for storing authentication details in the Spring Security context, allowing access to the user's unique ID.
 *
 * @property userId The unique identifier of the authenticated user.
 * @param principal The principal (typically username) associated with the authentication.
 * @param credentials The credentials (such as password), or null if not used.
 * @param authorities The authorities granted to the user.
 */
class CustomAuthenticationToken(
    val userId: Long,
    principal: Any,
    credentials: Any?,
    authorities: Collection<GrantedAuthority>
) : UsernamePasswordAuthenticationToken(principal, credentials, authorities)