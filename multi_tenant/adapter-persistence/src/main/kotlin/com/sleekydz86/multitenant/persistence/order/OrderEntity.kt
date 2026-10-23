package com.sleekydz86.multitenant.persistence.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.TenantId

@Entity
@Table(
    name = "orders",
    uniqueConstraints = [UniqueConstraint(columnNames = ["tenant_id", "order_id"])],
)
class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    var tenantId: String? = null

    @Column(name = "order_id", nullable = false)
    lateinit var orderId: String

    @Column(name = "status", nullable = false)
    lateinit var status: String
}
