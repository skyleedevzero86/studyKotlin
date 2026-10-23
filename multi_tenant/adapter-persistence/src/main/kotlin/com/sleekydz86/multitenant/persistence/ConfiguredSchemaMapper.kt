package com.sleekydz86.multitenant.persistence

import com.sleekydz86.multitenant.domain.UnknownTenant
import org.hibernate.context.spi.TenantSchemaMapper

class ConfiguredSchemaMapper(
    private val schemas: Map<String, String>,
) : TenantSchemaMapper<String> {
    override fun schemaName(tenantIdentifier: String): String =
        schemas[tenantIdentifier] ?: throw UnknownTenant(tenantIdentifier)
}
