package com.sleekydz86.multitenant.bootstrap

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer

object SharedPostgres {
    val container: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17-alpine")
        .withDatabaseName("multitenant")
        .withUsername("multitenant")
        .withPassword("multitenant")

    init {
        container.start()
        listOf("multitenant_marvel", "multitenant_dc", "multitenant_metadata").forEach { name ->
            val created = container.execInContainer(
                "psql", "-U", container.username, "-d", "postgres",
                "-c", "CREATE DATABASE $name",
            )
            check(created.exitCode == 0) { created.stderr }
        }
    }

    fun jdbcUrl(database: String): String =
        container.jdbcUrl.substringBeforeLast("/") + "/" + database

    fun registerUser(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.username") { container.username }
        registry.add("spring.datasource.password") { container.password }
        registry.add("demo.seed") { "false" }
    }
}
