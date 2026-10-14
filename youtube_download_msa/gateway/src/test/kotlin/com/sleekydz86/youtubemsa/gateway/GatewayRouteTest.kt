package com.sleekydz86.youtubemsa.gateway

import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@DisplayName("단위: 게이트웨이 경로")
class GatewayRouteTest {
    @Test
    @DisplayName("목록과 추천과 반응은 검색 서비스로, 저장 요청은 다운로드 서비스로 보낸다")
    fun splitsByPath() {
        // given
        val list = upstream("GET", "/api/downloads")
        val saved = upstream("GET", "/api/downloads/saved?q=등대")
        val recommend = upstream("GET", "/api/downloads/recommendations")
        val one = upstream("GET", "/api/downloads/4/recommendations")
        val similar = upstream("POST", "/api/downloads/search")
        val paste = upstream("POST", "/api/downloads")
        val like = upstream("POST", "/api/downloads/4/likes")

        // when
        val internal = upstream("GET", "/internal/downloads/4")

        // then
        assertEquals(Upstream.SEARCH, list)
        assertEquals(Upstream.SEARCH, saved)
        assertEquals(Upstream.SEARCH, recommend)
        assertEquals(Upstream.SEARCH, one)
        assertEquals(Upstream.SEARCH, similar)
        assertEquals(Upstream.DOWNLOAD, paste)
        assertEquals(Upstream.SEARCH, like)
        assertNull(internal)
    }
}
