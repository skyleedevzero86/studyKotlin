package com.sleekydz86.multitenant.application

import com.sleekydz86.multitenant.domain.Order
import com.sleekydz86.multitenant.domain.OrderStore

class MemoryOrders : OrderStore {
    private val rows = mutableListOf<Order>()

    override fun findByOrderId(orderId: String): Order? = rows.find { it.orderId == orderId }

    override fun findNative(orderId: String): List<Order> = rows.filter { it.orderId == orderId }

    override fun save(order: Order): Order {
        rows += order
        return order
    }
}
