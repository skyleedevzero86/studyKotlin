package com.sleekydz86.cdcmigration.global.procedure

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class Worker(val name: String, val ip: String) {
    companion object {
        val SYSTEM = Worker("SYSTEM", "127.0.0.1")
        val CDC = Worker("CDC", "debezium")
        val BATCH = Worker("BATCH", "127.0.0.1")
    }
}

object WorkerContext {

    private val current = ThreadLocal<Worker?>()

    fun <T> runAs(worker: Worker, block: () -> T): T {
        val previous = current.get()
        current.set(worker)
        try {
            return block()
        } finally {
            current.set(previous)
        }
    }

    fun current(): Worker = current.get() ?: fromRequest() ?: Worker.SYSTEM

    private fun fromRequest(): Worker? {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request ?: return null
        val name = request.getHeader("X-Worker")
            ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8) }
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "관리자"
        val forwarded = request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
        val ip = when (val address = forwarded ?: request.remoteAddr) {
            "0:0:0:0:0:0:0:1", "::1" -> "127.0.0.1"
            else -> address
        }
        return Worker(name, ip)
    }
}
