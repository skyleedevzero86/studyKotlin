package com.sleekydz86.multitenant.domain

data class Order(
    val id: Long?,
    val tenantId: String,
    val orderId: String,
    val status: String,
)
