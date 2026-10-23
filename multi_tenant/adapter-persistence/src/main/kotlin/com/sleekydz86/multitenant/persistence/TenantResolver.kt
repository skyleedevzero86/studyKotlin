package com.sleekydz86.multitenant.persistence

import com.sleekydz86.multitenant.domain.KnownTenants
import com.sleekydz86.multitenant.domain.MissingTenant
import com.sleekydz86.multitenant.domain.TenantContext
import com.sleekydz86.multitenant.domain.UnknownTenant
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.slf4j.LoggerFactory

class TenantResolver(
    private val tenants: KnownTenants,
) : CurrentTenantIdentifierResolver<String> {
    override fun resolveCurrentTenantIdentifier(): String {
        if (!TenantContext.bound()) {
            return tenants.ids().firstOrNull() ?: throw MissingTenant()
        }
        val tenant = TenantContext.required()
        if (!tenants.knows(tenant)) {
            log.warn("등록되지 않은 테넌트입니다. tenant={}", tenant)
            throw UnknownTenant(tenant)
        }
        return tenant
    }

    override fun validateExistingCurrentSessions(): Boolean = false

    private companion object {
        val log = LoggerFactory.getLogger(TenantResolver::class.java)
    }
}
