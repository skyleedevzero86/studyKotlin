package com.sleekydz86.komfa.domain.auth

sealed interface OttDeliveryResult {
    data object Sent : OttDeliveryResult
    data class Failed(val reason: String) : OttDeliveryResult
}
