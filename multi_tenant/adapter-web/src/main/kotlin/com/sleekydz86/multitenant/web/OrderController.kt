package com.sleekydz86.multitenant.web

import com.sleekydz86.multitenant.application.FindOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(prefix = "demo", name = ["strategy"], havingValue = "ROW")
class OrderController(private val findOrder: FindOrder) {
    @GetMapping("/orders/{orderId}")
    fun get(@PathVariable orderId: String): OrderView {
        val order = findOrder.find(orderId)
        return OrderView(order.orderId, order.status, order.id, order.tenantId)
    }
}
