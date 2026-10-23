package com.sleekydz86.multitenant.web

data class OrderView(
    val orderId: String,
    val status: String,
    val id: Long?,
    val tenantId: String,
)
