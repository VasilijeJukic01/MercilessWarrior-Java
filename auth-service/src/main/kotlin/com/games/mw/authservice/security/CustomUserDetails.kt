package com.games.mw.authservice.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUserDetails(
    val id: Long,
    username: String,
    password: String,
    authorities: Collection<GrantedAuthority>
) : User(username, password, authorities)