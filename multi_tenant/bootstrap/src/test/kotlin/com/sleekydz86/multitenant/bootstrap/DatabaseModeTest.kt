package com.sleekydz86.multitenant.bootstrap

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@ActiveProfiles("database")
class DatabaseModeTest : CastingModeTest() {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun register(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { SharedPostgres.jdbcUrl("multitenant_metadata") }
            SharedPostgres.registerUser(registry)
            registry.add("demo.database.username") { SharedPostgres.container.username }
            registry.add("demo.database.password") { SharedPostgres.container.password }
            registry.add("demo.database.datasources.marvel.url") { SharedPostgres.jdbcUrl("multitenant_marvel") }
            registry.add("demo.database.datasources.dc.url") { SharedPostgres.jdbcUrl("multitenant_dc") }
        }
    }
}
