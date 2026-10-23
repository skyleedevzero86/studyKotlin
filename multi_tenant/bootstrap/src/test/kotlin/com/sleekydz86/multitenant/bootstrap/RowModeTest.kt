package com.sleekydz86.multitenant.bootstrap

import com.sleekydz86.multitenant.domain.Order
import com.sleekydz86.multitenant.domain.OrderStore
import com.sleekydz86.multitenant.domain.TenantContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("row")
class RowModeTest(
    @Autowired private val orders: OrderStore,
    @LocalServerPort private val port: Int,
) {
    private val http = HttpClient.newHttpClient()

    @BeforeEach
    fun prepare() {
        save("bookshop", "PAID")
        save("recordshop", "PENDING")
    }

    @Test
    @DisplayName("같은 주문 번호도 헤더의 테넌트 행만 돌려준다")
    fun headerSelectsTheRow() {
        val bookshop = get("/orders/ORDER-001", "bookshop")
        val recordshop = get("/orders/ORDER-001", "recordshop")
        assertEquals(200, bookshop.statusCode())
        assertTrue(bookshop.body().contains("\"status\":\"PAID\""))
        assertTrue(bookshop.body().contains("\"tenantId\":\"bookshop\""))
        assertEquals(200, recordshop.statusCode())
        assertTrue(recordshop.body().contains("\"status\":\"PENDING\""))
        assertTrue(recordshop.body().contains("\"tenantId\":\"recordshop\""))
    }

    @Test
    @DisplayName("다른 테넌트 주문은 없는 것으로 답한다")
    fun hidesTheOtherTenant() {
        val missing = get("/orders/ORDER-404", "bookshop")
        assertEquals(404, missing.statusCode())
    }

    @Test
    @DisplayName("헤더가 없으면 400")
    fun missingHeader() {
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port/orders/ORDER-001")).GET().build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(400, response.statusCode())
    }

    @Test
    @DisplayName("등록되지 않은 테넌트는 400")
    fun unknownTenant() {
        assertEquals(400, get("/orders/ORDER-001", "other").statusCode())
    }

    @Test
    @DisplayName("네이티브 SQL 에는 테넌트 조건이 붙지 않는다")
    fun nativeQuerySeesEveryTenant() {
        val leaked = TenantContext.call("bookshop") { orders.findNative("ORDER-001") }
        assertEquals(setOf("bookshop", "recordshop"), leaked.map { it.tenantId }.toSet())
        val safe = TenantContext.call("bookshop") { orders.findByOrderId("ORDER-001") }
        assertEquals("PAID", safe?.status)
    }

    private fun save(tenant: String, status: String) {
        TenantContext.call(tenant) {
            if (orders.findByOrderId("ORDER-001") == null) {
                orders.save(Order(null, tenant, "ORDER-001", status))
            }
        }
    }

    private fun get(path: String, tenant: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path"))
            .header("X-Tenant-Id", tenant)
            .GET()
            .build()
        return http.send(request, HttpResponse.BodyHandlers.ofString())
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun register(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { SharedPostgres.jdbcUrl("multitenant") }
            SharedPostgres.registerUser(registry)
        }
    }
}
