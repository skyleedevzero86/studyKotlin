package com.karchitecture.helper

import com.karchitecture.auth.application.AuthService
import com.karchitecture.auth.application.TokenProvider
import com.karchitecture.global.config.auth.support.AuthenticationContext
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.test.context.bean.override.mockito.MockitoBean

class MockBeanInjection() {

    @MockitoBean
    protected lateinit var jpaMetamodelMappingContext: JpaMetamodelMappingContext

    @MockitoBean
    protected lateinit var tokenProvider: TokenProvider

    @MockitoBean
    protected lateinit var authenticationContext: AuthenticationContext

    @MockitoBean
    protected lateinit var authService: AuthService
}