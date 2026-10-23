package com.sleekydz86.multitenant.domain

class OrderNotFound(val orderId: String) :
    RuntimeException("주문을 찾을 수 없습니다. orderId=$orderId")
