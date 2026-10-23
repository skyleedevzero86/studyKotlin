package com.sleekydz86.multitenant.persistence.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    fun findByOrderId(orderId: String): OrderEntity?

    @Query(value = "select * from orders where order_id = :orderId", nativeQuery = true)
    fun findNativeByOrderId(orderId: String): List<OrderEntity>
}
