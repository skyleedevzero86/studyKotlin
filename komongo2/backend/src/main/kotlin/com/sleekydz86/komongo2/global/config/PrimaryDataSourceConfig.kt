package com.sleekydz86.komongo2.global.config

import com.sleekydz86.komongo2.itemlog.domain.ItemLog
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.sleekydz86.komongo2.itemlog.primary"],
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
class PrimaryDataSourceConfig {

    @Primary
    @Bean("primaryDataSourceProperties")
    @ConfigurationProperties("spring.datasource.primary")
    fun primaryDataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Primary
    @Bean("primaryDataSource")
    fun primaryDataSource(@Qualifier("primaryDataSourceProperties") props: DataSourceProperties): DataSource {
        return props.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }

    @Primary
    @Bean("primaryEntityManagerFactory")
    fun primaryEntityManagerFactory(@Qualifier("primaryDataSource") dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter = HibernateJpaVendorAdapter()
        vendorAdapter.setGenerateDdl(true)
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        factory.setManagedTypes(PersistenceManagedTypes.of(ItemLog::class.java.name))
        factory.jpaVendorAdapter = vendorAdapter
        factory.setPersistenceUnitName("primary")
        return factory
    }

    @Primary
    @Bean("primaryTransactionManager")
    fun primaryTransactionManager(
        @Qualifier("primaryEntityManagerFactory") factory: LocalContainerEntityManagerFactoryBean
    ): PlatformTransactionManager {
        return JpaTransactionManager(factory.`object`!!)
    }
}
