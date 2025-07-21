package com.kominioai.domain.auth.application.dto

data class AdminUserListRequest(
    val searchQuery: String? = null,
    val accountStatus: String? = null,
    val emailVerified: Boolean? = null,
    val twoFactorEnabled: Boolean? = null,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
    val page: Int = 1,
    val size: Int = 20
)