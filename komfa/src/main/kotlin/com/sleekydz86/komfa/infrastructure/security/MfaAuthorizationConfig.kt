package com.sleekydz86.komfa.infrastructure.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication
import org.springframework.security.core.authority.FactorGrantedAuthority

@Configuration
@EnableMultiFactorAuthentication(
    authorities = [
        FactorGrantedAuthority.PASSWORD_AUTHORITY,
        FactorGrantedAuthority.OTT_AUTHORITY
    ]
)
class MfaAuthorizationConfig
