package com.sleekydz86.komfa.application.auth

import com.sleekydz86.komfa.domain.auth.OttDeliveryResult
import com.sleekydz86.komfa.domain.auth.TokenValue
import com.sleekydz86.komfa.domain.auth.Username

fun interface OttDeliveryPort {
    fun deliver(username: Username, token: TokenValue): OttDeliveryResult
}
