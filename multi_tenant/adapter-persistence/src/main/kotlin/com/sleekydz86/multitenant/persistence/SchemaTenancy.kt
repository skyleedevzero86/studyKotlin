package com.sleekydz86.multitenant.persistence

import com.sleekydz86.multitenant.persistence.casting.CastingEntity
import com.sleekydz86.multitenant.persistence.casting.CastingJpaRepository
import org.flywaydb.core.Flyway
import org.hibernate.cfg.MultiTenancySettings
import org.hibernate.context.spi.TenantSchemaMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(prefix = "demo", name = ["strategy"], havingValue = "SCHEMA")
@EnableJpaRepositories(basePackageClasses = [CastingJpaRepository::class])
@EntityScan(basePackageClasses = [CastingEntity::class])
class SchemaTenancy(private val demo: DemoProperties) {
    @Bean
    fun migratedSchemas(dataSource: DataSource): MigratedSchemas {
        demo.schema.tenants.values.forEach { schema ->
            Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .createSchemas(true)
                .locations("classpath:db/migration/casting")
                .load()
                .migrate()
        }
        return MigratedSchemas
    }

    @Bean
    fun schemaMapper(): TenantSchemaMapper<String> = ConfiguredSchemaMapper(demo.schema.tenants)

    @Bean
    fun schemaRouting(
        mapper: TenantSchemaMapper<String>,
        migrated: MigratedSchemas,
    ): HibernatePropertiesCustomizer = HibernatePropertiesCustomizer { properties ->
        check(migrated == MigratedSchemas)
        properties[MultiTenancySettings.MULTI_TENANT_SCHEMA_MAPPER] = mapper
    }
}
