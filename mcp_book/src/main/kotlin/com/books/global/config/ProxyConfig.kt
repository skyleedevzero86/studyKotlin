package com.books.global.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

/**
 * 프록시 설정 클래스
 */
@Configuration
class ProxyConfig {

    // 프록시 설정
    private val proxyHost = "127.0.0.1"
    private val proxyPort = 10080

    @PostConstruct
    fun setSystemProxy() {
        // 시스템 프록시 속성 설정, 이는 Spring Boot의 자동 구성 HTTP 클라이언트에 영향을 줍니다.
        System.setProperty("http.proxyHost", proxyHost)
        System.setProperty("http.proxyPort", proxyPort.toString())
        System.setProperty("https.proxyHost", proxyHost)
        System.setProperty("https.proxyPort", proxyPort.toString())

        println("System proxy configured: http://$proxyHost:$proxyPort")
    }
}
