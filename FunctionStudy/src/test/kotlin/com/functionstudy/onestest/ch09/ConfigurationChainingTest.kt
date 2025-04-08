package com.functionstudy.onestest.ch09

import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class ConfigurationChainingTest {

    // Reader 타입 정의 (Functional Programming 스타일)
    class ConfigurationReader<T>(val runWith: (Map<String, String>) -> T) {
        fun <U> map(f: (T) -> U): ConfigurationReader<U> =
            ConfigurationReader { config -> f(runWith(config)) }

        fun <U> flatMap(f: (T) -> ConfigurationReader<U>): ConfigurationReader<U> =
            ConfigurationReader { config -> f(runWith(config)).runWith(config) }
    }

    // 확장 함수로 bind 구현
    fun <T, U> ConfigurationReader<T>.bind(f: (T) -> ConfigurationReader<U>): ConfigurationReader<U> = this.flatMap(f)

    // environment 값을 읽는 Reader
    val env = ConfigurationReader { ctx: Map<String, String> -> ctx["environment"] }

    // serverHost 값을 읽는 Reader
    fun serverHost(env: String): ConfigurationReader<String?> =
        ConfigurationReader { ctx -> ctx["$env-server"] }

    // 테스트
    @Test
    fun configurationChainingEnvAndPort() {
        val portReader = { host: String ->
            ConfigurationReader { ctx: Map<String, String> -> ctx["$host-port"] }
        }

        val res = env
            .bind { envVal -> serverHost(envVal ?: "local") }
            .bind { host -> ConfigurationReader { ctx -> ctx["${host ?: "localhost"}-port"] } }
            .runWith(
                mapOf(
                    "environment" to "local",
                    "local-server" to "localhost",
                    "localhost-port" to "8080"
                )
            )

        expectThat(res).isEqualTo("8080")
    }
}
