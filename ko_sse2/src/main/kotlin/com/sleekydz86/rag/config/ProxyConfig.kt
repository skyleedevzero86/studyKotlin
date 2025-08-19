package com.sleekydz86.rag.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class ProxyConfig {

    companion object {
        private const val PROXY_HOST = "127.0.0.1"
        private const val PROXY_PORT = 10080
    }

    @PostConstruct
    fun setSystemProxy() {
        listOf("http", "https").forEach { protocol ->
            System.setProperty("${protocol}.proxyHost", PROXY_HOST)
            System.setProperty("${protocol}.proxyPort", PROXY_PORT.toString())
        }

        println("시스템 프록시 설정됨: http://$PROXY_HOST:$PROXY_PORT")
    }
}