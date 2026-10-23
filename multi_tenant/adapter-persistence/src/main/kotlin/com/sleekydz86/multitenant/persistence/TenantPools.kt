package com.sleekydz86.multitenant.persistence

import com.sleekydz86.multitenant.domain.UnknownTenant
import com.zaxxer.hikari.HikariDataSource
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl
import javax.sql.DataSource

class TenantPools(
    private val pools: Map<String, HikariDataSource>,
    private val metadata: DataSource,
) : AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String>(), AutoCloseable {
    override fun selectDataSource(tenantIdentifier: String): DataSource =
        pools[tenantIdentifier] ?: throw UnknownTenant(tenantIdentifier)

    override fun selectAnyDataSource(): DataSource = metadata

    override fun close() {
        pools.values.forEach { it.close() }
    }
}
