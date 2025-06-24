package com.games.mw.gameservice.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class CustomAuthenticationToken(
    val userId: Long,
    principal: Any,
    credentials: Any?,
    authorities: Collection<GrantedAuthority>
) : UsernamePasswordAuthenticationToken(principal, credentials, authorities)