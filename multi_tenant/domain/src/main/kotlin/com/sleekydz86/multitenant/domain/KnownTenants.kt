package com.sleekydz86.multitenant.domain

interface KnownTenants {
    fun knows(tenant: String): Boolean

    fun ids(): List<String>
}
