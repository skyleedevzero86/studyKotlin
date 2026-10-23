package com.sleekydz86.multitenant.application

import com.sleekydz86.multitenant.domain.Order
import com.sleekydz86.multitenant.domain.OrderNotFound
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FindOrderTest {
    @Test
    @DisplayName("없는 주문은 테넌트 밖 존재 여부를 말하지 않는다")
    fun hidesMissingOrder() {
        val finder = FindOrder(MemoryOrders())
        val error = assertFailsWith<OrderNotFound> { finder.find("ORDER-001") }
        assertEquals("ORDER-001", error.orderId)
    }

    @Test
    @DisplayName("저장소에 있는 주문만 돌려준다")
    fun returnsStoredOrder() {
        val orders = MemoryOrders()
        orders.save(Order(1, "bookshop", "ORDER-001", "PAID"))
        val found = FindOrder(orders).find("ORDER-001")
        assertEquals("PAID", found.status)
    }
}
