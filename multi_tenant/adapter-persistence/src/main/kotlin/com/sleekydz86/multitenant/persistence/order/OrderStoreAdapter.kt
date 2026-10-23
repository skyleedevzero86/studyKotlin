package com.sleekydz86.multitenant.persistence.order

import com.sleekydz86.multitenant.domain.Order
import com.sleekydz86.multitenant.domain.OrderStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@ConditionalOnProperty(prefix = "demo", name = ["strategy"], havingValue = "ROW")
class OrderStoreAdapter(private val orders: OrderJpaRepository) : OrderStore {
    @Transactional(readOnly = true)
    override fun findByOrderId(orderId: String): Order? = orders.findByOrderId(orderId)?.toOrder()

    @Transactional(readOnly = true)
    override fun findNative(orderId: String): List<Order> =
        orders.findNativeByOrderId(orderId).map { it.toOrder() }

    @Transactional
    override fun save(order: Order): Order {
        val entity = OrderEntity().apply {
            orderId = order.orderId
            status = order.status
        }
        return orders.save(entity).toOrder()
    }
}

private fun OrderEntity.toOrder() = Order(
    id = id,
    tenantId = tenantId ?: "",
    orderId = orderId,
    status = status,
)
