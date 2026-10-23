package com.sleekydz86.multitenant.persistence

import com.sleekydz86.multitenant.persistence.casting.CastingEntity
import com.sleekydz86.multitenant.persistence.casting.CastingJpaRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.hibernate.cfg.MultiTenancySettings
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(prefix = "demo", name = ["strategy"], havingValue = "DATABASE")
@EnableJpaRepositories(basePackageClasses = [CastingJpaRepository::class])
@EntityScan(basePackageClasses = [CastingEntity::class])
class DatabaseTenancy {
    @Bean
    fun tenantPools(demo: DemoProperties, metadata: DataSource): TenantPools {
        val pools = demo.database.datasources.mapValues { (tenant, source) ->
            val pool = hikari(source.url, demo.database.username, demo.database.password, tenant)
            Flyway.configure()
                .dataSource(pool)
                .locations("classpath:db/migration/casting")
                .load()
                .migrate()
            pool
        }
        return TenantPools(pools, metadata)
    }

    @Bean
    fun databaseRouting(pools: TenantPools): HibernatePropertiesCustomizer =
        HibernatePropertiesCustomizer { properties ->
            properties[MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER] = pools
        }
}

private fun hikari(url: String, username: String, password: String, name: String): HikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = url
            this.username = username
            this.password = password
            poolName = "tenant-$name"
            maximumPoolSize = 3
        },
    )
