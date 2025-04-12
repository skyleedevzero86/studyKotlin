package com.functionstudy.ones.ch09.controller

import com.functionstudy.ones.ch09.core.result.ConfigurationReader

class ConfigurationChainingController {
    fun execute() {

        // 포트 값을 읽는 함수
        val portReader = { host: String ->
            ConfigurationReader { ctx: Map<String, String> -> ctx["$host-port"] }
        }

        // 환경 설정 값을 담은 맵
        val config = mapOf(
            "environment" to "local",
            "local-server" to "localhost",
            "localhost-port" to "8080"
        )

        // 환경, 서버 호스트, 포트 값을 연쇄적으로 읽음
        val res = env
            .bind { envVal ->
                println("환경: $envVal")
                serverHost(envVal ?: "local")
            }
            .bind { host ->
                println("호스트: $host")
                ConfigurationReader { ctx -> ctx["${host ?: "localhost"}-port"] }
            }
            .runWith(config)

        println("결론: $res")

        if (res == "8080") {
            println("테스트 성공: 결과는 8080입니다.")
        } else {
            println("테스트 실패: 예상한 값이 아닙니다. 결과는 $res 입니다.")
        }
    }
}

fun <T, U> ConfigurationReader<T>.bind(f: (T) -> ConfigurationReader<U>): ConfigurationReader<U> = this.flatMap(f)

// 환경값을 읽음처리
val env = ConfigurationReader { ctx: Map<String, String> -> ctx["environment"] }

// 서버 호스트 값을 읽는 함수
fun serverHost(env: String): ConfigurationReader<String?> =
    ConfigurationReader { ctx -> ctx["$env-server"] }

fun main() {
    val controller = ConfigurationChainingController()
    controller.execute()  // 결과 출력
}