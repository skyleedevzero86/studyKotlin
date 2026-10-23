package com.sleekydz86.multitenant.bootstrap

import com.sleekydz86.multitenant.domain.Order
import com.sleekydz86.multitenant.domain.OrderStore
import com.sleekydz86.multitenant.domain.TenantContext
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "demo", name = ["seed"], havingValue = "true")
@ConditionalOnProperty(prefix = "demo", name = ["strategy"], havingValue = "ROW")
class RowSeed(private val orders: OrderStore) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        save("bookshop", "PAID")
        save("recordshop", "PENDING")
    }

    private fun save(tenant: String, status: String) {
        TenantContext.call(tenant) {
            if (orders.findByOrderId("ORDER-001") == null) {
                orders.save(Order(null, tenant, "ORDER-001", status))
            }
        }
    }
}
