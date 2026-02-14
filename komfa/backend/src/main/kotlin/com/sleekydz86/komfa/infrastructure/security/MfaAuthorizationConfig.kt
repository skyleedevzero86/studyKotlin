package com.sleekydz86.komfa.infrastructure.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication

@Configuration
@EnableMultiFactorAuthentication(authorities = [])
class MfaAuthorizationConfig
