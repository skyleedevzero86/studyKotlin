package com.sleekydz86.multitenant.persistence

import com.sleekydz86.multitenant.domain.KnownTenants
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "demo")
data class DemoProperties(
    val strategy: String = "",
    val seed: Boolean = false,
    val row: Row = Row(),
    val schema: Schema = Schema(),
    val database: Database = Database(),
) : KnownTenants {
    override fun knows(tenant: String): Boolean = ids().contains(tenant)

    override fun ids(): List<String> = when (strategy) {
        "ROW" -> row.tenants
        "SCHEMA" -> schema.tenants.keys.toList()
        "DATABASE" -> database.datasources.keys.toList()
        else -> emptyList()
    }

    data class Row(val tenants: List<String> = emptyList())

    data class Schema(val tenants: Map<String, String> = emptyMap())

    data class Database(
        val username: String = "",
        val password: String = "",
        val datasources: Map<String, Source> = emptyMap(),
    )

    data class Source(val url: String = "")
}
