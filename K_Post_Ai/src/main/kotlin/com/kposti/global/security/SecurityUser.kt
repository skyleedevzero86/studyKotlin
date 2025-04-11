package com.kposti.global.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
    val id: Long,
    username: String,
    password: String,
    val nickname: String,
    authorities: Collection<GrantedAuthority>
) : User(username, password, authorities), OAuth2User {

    override fun getAttributes(): Map<String, Any> = emptyMap()

    override fun getName(): String = username
}
