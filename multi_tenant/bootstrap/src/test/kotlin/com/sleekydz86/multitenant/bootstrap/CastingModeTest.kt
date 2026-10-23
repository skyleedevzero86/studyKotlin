package com.sleekydz86.multitenant.bootstrap

import com.sleekydz86.multitenant.application.CastingComparison
import com.sleekydz86.multitenant.application.FindCasting
import com.sleekydz86.multitenant.domain.Casting
import com.sleekydz86.multitenant.domain.CastingStore
import com.sleekydz86.multitenant.domain.TenantContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class CastingModeTest {
    @Autowired
    lateinit var castings: CastingStore

    @Autowired
    lateinit var findCasting: FindCasting

    @Autowired
    lateinit var comparison: CastingComparison

    @LocalServerPort
    var port: Int = 0

    private val http = HttpClient.newHttpClient()

    @BeforeEach
    fun prepare() {
        save("marvel", "Deadpool")
        save("dc", "Green Lantern")
    }

    @Test
    @DisplayName("같은 배우는 테넌트마다 다른 배역을 돌려준다")
    fun sameActorReturnsDifferentCharacters() {
        val marvel = TenantContext.call("marvel") { findCasting.find("ryan-reynolds") }
        val dc = TenantContext.call("dc") { findCasting.find("ryan-reynolds") }
        assertEquals("Deadpool", marvel.characterName)
        assertEquals("Green Lantern", dc.characterName)
    }

    @Test
    @DisplayName("이미 열린 세션은 바깥 테넌트를 유지한다")
    fun openSessionKeepsItsTenant() {
        val stuck = TenantContext.call("marvel") { comparison.sharingSession("ryan-reynolds") }
        assertEquals("Deadpool", stuck["marvel"])
        assertEquals("Deadpool", stuck["dc"])
    }

    @Test
    @DisplayName("테넌트마다 새 세션을 열면 배역이 갈린다")
    fun newSessionReadsEachTenant() {
        val fresh = TenantContext.call("marvel") { comparison.newSessions("ryan-reynolds") }
        assertEquals("Deadpool", fresh["marvel"])
        assertEquals("Green Lantern", fresh["dc"])
    }

    @Test
    @DisplayName("헤더만 바꿔도 배역이 갈린다")
    fun headerSelectsTheStore() {
        val marvel = get("marvel")
        val dc = get("dc")
        assertEquals(200, marvel.statusCode())
        assertTrue(marvel.body().contains("\"characterName\":\"Deadpool\""))
        assertEquals(200, dc.statusCode())
        assertTrue(dc.body().contains("\"characterName\":\"Green Lantern\""))
    }

    private fun save(tenant: String, character: String) {
        TenantContext.call(tenant) {
            if (castings.findByActor("ryan-reynolds") == null) {
                castings.save(Casting("ryan-reynolds", character))
            }
        }
    }

    private fun get(tenant: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port/castings/ryan-reynolds"))
            .header("X-Tenant-Id", tenant)
            .GET()
            .build()
        return http.send(request, HttpResponse.BodyHandlers.ofString())
    }
}
