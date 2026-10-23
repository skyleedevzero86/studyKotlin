package com.sleekydz86.multitenant.domain

interface OrderStore {
    fun findByOrderId(orderId: String): Order?

    fun findNative(orderId: String): List<Order>

    fun save(order: Order): Order
}
