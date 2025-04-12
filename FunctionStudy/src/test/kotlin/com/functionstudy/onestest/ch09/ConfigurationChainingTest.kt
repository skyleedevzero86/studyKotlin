package com.functionstudy.onestest.ch09

import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class ConfigurationChainingTest {

    // Reader 타입 정의 함수형 스타일
    class ConfigurationReader<T>(val runWith: (Map<String, String>) -> T) {
        fun <U> map(f: (T) -> U): ConfigurationReader<U> =
            ConfigurationReader { config -> f(runWith(config)) }

        fun <U> flatMap(f: (T) -> ConfigurationReader<U>): ConfigurationReader<U> =
            ConfigurationReader { config -> f(runWith(config)).runWith(config) }
    }

    // 확장 함수로 구현
    fun <T, U> ConfigurationReader<T>.bind(f: (T) -> ConfigurationReader<U>): ConfigurationReader<U> = this.flatMap(f)

    // environment 값을 읽다
    val env = ConfigurationReader { ctx: Map<String, String> -> ctx["environment"] }

    // serverHost 값을 읽다
    fun serverHost(env: String): ConfigurationReader<String?> =
        ConfigurationReader { ctx -> ctx["$env-server"] }


    @Test
    fun configurationChainingEnvAndPort() {
        val portReader = { host: String ->
            ConfigurationReader { ctx: Map<String, String> -> ctx["$host-port"] }
        }

        // Given
        val config = mapOf(
            "environment" to "local",
            "local-server" to "localhost",
            "localhost-port" to "8080"
        )

        // When
        val res = env
            .bind { envVal ->
                println("Given environment: $envVal")
                serverHost(envVal ?: "local")
            }
            .bind { host ->
                println("Given host: $host")
                ConfigurationReader { ctx -> ctx["${host ?: "localhost"}-port"] }
            }
            .runWith(config)

        // Then
        println("결론: $res")
        expectThat(res).isEqualTo("8080")
    }
}
