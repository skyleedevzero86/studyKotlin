package com.sleekydz86.multitenant.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TenantContextTest {
    @Test
    @DisplayName("바인딩한 범위에서만 테넌트를 읽는다")
    fun bindsForTheCall() {
        val seen = TenantContext.call("marvel") { TenantContext.required() }
        assertEquals("marvel", seen)
        assertFalse(TenantContext.bound())
        TenantContext.call("dc") { assertTrue(TenantContext.bound()) }
        assertFailsWith<MissingTenant> { TenantContext.required() }
    }
}
