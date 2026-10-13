package com.sleekydz86.densevector.integration

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mysql.MySQLContainer

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PassageApiIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Order(1)
    fun emptyDatabaseStaysEmptyUntilAPassageIsCreated() {
        mockMvc.perform(get("/api/passages"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(0))
    }

    @Test
    fun createGetSearchUpdateDelete() {
        val created = mockMvc.perform(
            post("/api/passages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"통합 검증 문단","body":"코사인 유사도는 방향이 비슷한 정도를 재는 학습용 문장이다."}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.dims").value(64))
            .andExpect(jsonPath("$.embedding.length()").value(64))
            .andReturn()
        val id = com.jayway.jsonpath.JsonPath.read<Int>(created.response.contentAsString, "$.id")

        mockMvc.perform(get("/api/passages/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("통합 검증 문단"))

        mockMvc.perform(
            post("/api/passages/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"query":"코사인 유사도는 방향","k":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hits[*].title", hasItem("통합 검증 문단")))

        mockMvc.perform(
            put("/api/passages/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"통합 검증 문단 수정","body":"수정된 학습용 문장이다."}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("통합 검증 문단 수정"))

        mockMvc.perform(delete("/api/passages/$id"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/api/passages/$id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", containsString("id=$id")))
    }

    @Test
    fun duplicateTitleReturns409() {
        val json = """{"title":"중복 제목","body":"첫 문단"}"""
        mockMvc.perform(post("/api/passages").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/passages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"중복 제목","body":"둘째 문단"}""")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message", containsString("같은 제목의 문단이 이미 있습니다.")))
    }

    companion object {
        @Container
        @JvmStatic
        val mysql = MySQLContainer("mysql:8.4")

        @Container
        @JvmStatic
        val elasticsearch = ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.15.3")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.enrollment.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withEnv("xpack.security.transport.ssl.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                mysql.jdbcUrl + "?characterEncoding=UTF-8&serverTimezone=Asia/Seoul"
            }
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("study.vector.elasticsearch-url") { "http://${elasticsearch.httpHostAddress}" }
            registry.add("study.vector.index-name") { "it-passages" }
            registry.add("study.vector.startup-seed") { "false" }
        }
    }
}
