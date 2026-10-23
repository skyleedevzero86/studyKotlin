package com.sleekydz86.multitenant.domain

import java.lang.ScopedValue

object TenantContext {
    private val current: ScopedValue<String> = ScopedValue.newInstance()

    fun <T> call(tenant: String, work: () -> T): T =
        ScopedValue.where(current, tenant).call(ScopedValue.CallableOp<T, RuntimeException> { work() })

    fun bound(): Boolean = current.isBound

    fun required(): String = current.orElseThrow { MissingTenant() }
}
