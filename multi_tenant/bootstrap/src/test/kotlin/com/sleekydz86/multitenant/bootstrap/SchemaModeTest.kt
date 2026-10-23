package com.sleekydz86.multitenant.bootstrap

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@ActiveProfiles("schema")
class SchemaModeTest : CastingModeTest() {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun register(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { SharedPostgres.jdbcUrl("multitenant") }
            SharedPostgres.registerUser(registry)
        }
    }
}
