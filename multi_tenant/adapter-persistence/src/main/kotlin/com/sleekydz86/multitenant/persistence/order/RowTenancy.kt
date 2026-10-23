package com.sleekydz86.multitenant.persistence.order

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ConditionalOnProperty(prefix = "demo", name = ["strategy"], havingValue = "ROW")
@EnableJpaRepositories(basePackageClasses = [OrderJpaRepository::class])
@EntityScan(basePackageClasses = [OrderEntity::class])
class RowTenancy
