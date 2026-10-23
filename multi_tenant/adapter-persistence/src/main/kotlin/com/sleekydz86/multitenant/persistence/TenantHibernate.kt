package com.sleekydz86.multitenant.persistence

import com.sleekydz86.multitenant.domain.KnownTenants
import org.hibernate.cfg.MultiTenancySettings
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DemoProperties::class)
class TenantHibernate(private val tenants: KnownTenants) {
    @Bean
    fun tenantResolver(): CurrentTenantIdentifierResolver<String> = TenantResolver(tenants)

    @Bean
    fun tenantIdentity(resolver: CurrentTenantIdentifierResolver<String>): HibernatePropertiesCustomizer =
        HibernatePropertiesCustomizer { properties ->
            properties[MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER] = resolver
        }
}
