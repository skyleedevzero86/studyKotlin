package com.sleekydz86.multitenant.application

import com.sleekydz86.multitenant.domain.Order
import com.sleekydz86.multitenant.domain.OrderNotFound
import com.sleekydz86.multitenant.domain.OrderStore
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Conditional(RowStrategy::class)
class FindOrder(private val orders: OrderStore) {
    @Transactional(readOnly = true)
    fun find(orderId: String): Order =
        orders.findByOrderId(orderId) ?: throw OrderNotFound(orderId)
}
