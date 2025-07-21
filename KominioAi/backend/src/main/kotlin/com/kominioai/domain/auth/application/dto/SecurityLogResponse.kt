package com.kominioai.domain.auth.application.dto


data class SecurityLogResponse(
    val logs: List<SecurityLogEntry>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)