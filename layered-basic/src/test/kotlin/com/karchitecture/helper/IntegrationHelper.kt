package com.karchitecture.helper

import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationHelper() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun init() {
        RestAssured.port = port
        validateH2Database()
        val truncateAllTablesQuery = jdbcTemplate.queryForList(
            "SELECT CONCAT('TRUNCATE TABLE ', TABLE_NAME, ';') AS q FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
            String::class.java
        )
        truncateAllTables(truncateAllTablesQuery)
    }

    private fun validateH2Database() {
        jdbcTemplate.queryForObject("SELECT H2VERSION() FROM DUAL", String::class.java)
    }

    private fun truncateAllTables(truncateAllTablesQuery: List<String>) {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0")

        truncateAllTablesQuery.forEach { truncateQuery ->
            jdbcTemplate.execute(truncateQuery)
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1")
    }
}
