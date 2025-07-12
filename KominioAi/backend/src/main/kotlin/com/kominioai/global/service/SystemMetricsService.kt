package com.kominioai.global.service

import com.kominioai.global.util.MetricsUtils
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.lang.management.ManagementFactory
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * 시스템 메트릭 수집 서비스
 * 
 * @author KominioAI Team
 * @since 1.0.0
 */
@Service
class SystemMetricsService(
    private val meterRegistry: MeterRegistry,
    private val metricsUtils: MetricsUtils
) {
    
    private val logger = LoggerFactory.getLogger(SystemMetricsService::class.java)
    
    private val runtime = Runtime.getRuntime()
    private val memoryBean = ManagementFactory.getMemoryMXBean()
    private val threadBean = ManagementFactory.getThreadMXBean()
    
    private val requestCounter = AtomicLong(0)
    private val errorCounter = AtomicLong(0)
    private val activeConnections = AtomicLong(0)

    /**
     * 요청 카운터 증가
     */
    fun incrementRequestCounter() {
        requestCounter.incrementAndGet()
    }

    /**
     * 에러 카운터 증가
     */
    fun incrementErrorCounter() {
        errorCounter.incrementAndGet()
    }

    /**
     * 활성 연결 수 업데이트
     */
    fun updateActiveConnections(count: Long) {
        activeConnections.set(count)
    }

    /**
     * JVM 메모리 메트릭 수집 (매 30초마다)
     */
    @Scheduled(fixedRate = 30000) // 30초
    fun collectJvmMetrics() {
        val heapMemoryUsage = memoryBean.heapMemoryUsage
        val nonHeapMemoryUsage = memoryBean.nonHeapMemoryUsage

        // 힙 메모리 메트릭
        metricsUtils.recordSystemMetric("jvm.memory.heap.used", heapMemoryUsage.used.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.heap.committed", heapMemoryUsage.committed.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.heap.max", heapMemoryUsage.max.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.heap.usage.percent", 
            (heapMemoryUsage.used.toDouble() / heapMemoryUsage.max) * 100)

        // 비힙 메모리 메트릭
        metricsUtils.recordSystemMetric("jvm.memory.nonheap.used", nonHeapMemoryUsage.used.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.nonheap.committed", nonHeapMemoryUsage.committed.toDouble())

        // 시스템 메모리 메트릭
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()

        metricsUtils.recordSystemMetric("jvm.memory.system.total", totalMemory.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.system.free", freeMemory.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.system.used", usedMemory.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.system.max", maxMemory.toDouble())
        metricsUtils.recordSystemMetric("jvm.memory.system.usage.percent", (usedMemory.toDouble() / maxMemory) * 100)

        StructuredLogging.logDebug(
            logger,
            "JVM Memory Metrics Collected",
            "heapUsedMB" to (heapMemoryUsage.used / 1024 / 1024).toString(),
            "heapMaxMB" to (heapMemoryUsage.max / 1024 / 1024).toString(),
            "heapUsagePercent" to ((heapMemoryUsage.used.toDouble() / heapMemoryUsage.max) * 100).toString(),
            "systemUsedMB" to (usedMemory / 1024 / 1024).toString(),
            "systemMaxMB" to (maxMemory / 1024 / 1024).toString()
        )
    }

    /**
     * JVM 스레드 메트릭 수집 (매 30초마다)
     */
    @Scheduled(fixedRate = 30000) // 30초
    fun collectThreadMetrics() {
        val threadCount = threadBean.threadCount
        val peakThreadCount = threadBean.peakThreadCount
        val daemonThreadCount = threadBean.daemonThreadCount
        val totalStartedThreadCount = threadBean.totalStartedThreadCount

        metricsUtils.recordSystemMetric("jvm.threads.current", threadCount.toDouble())
        metricsUtils.recordSystemMetric("jvm.threads.peak", peakThreadCount.toDouble())
        metricsUtils.recordSystemMetric("jvm.threads.daemon", daemonThreadCount.toDouble())
        metricsUtils.recordSystemMetric("jvm.threads.total.started", totalStartedThreadCount.toDouble())

        StructuredLogging.logDebug(
            logger,
            "JVM Thread Metrics Collected",
            "currentThreads" to threadCount.toString(),
            "peakThreads" to peakThreadCount.toString(),
            "daemonThreads" to daemonThreadCount.toString(),
            "totalStartedThreads" to totalStartedThreadCount.toString()
        )
    }

    /**
     * GC 메트릭 수집 (매 1분마다)
     */
    @Scheduled(fixedRate = 60000) // 1분
    fun collectGcMetrics() {
        val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
        
        gcBeans.forEach { gcBean ->
            val name = gcBean.name
            val collectionCount = gcBean.collectionCount
            val collectionTime = gcBean.collectionTime

            metricsUtils.recordSystemMetric("jvm.gc.collections", collectionCount.toDouble(), 
                mapOf("gc" to name))
            metricsUtils.recordSystemMetric("jvm.gc.collection.time", collectionTime.toDouble(), 
                mapOf("gc" to name))
        }

        StructuredLogging.logDebug(
            logger,
            "GC Metrics Collected",
            "gcCount" to gcBeans.size.toString(),
            "timestamp" to LocalDateTime.now().toString()
        )
    }

    /**
     * 애플리케이션 메트릭 수집 (매 1분마다)
     */
    @Scheduled(fixedRate = 60000) // 1분
    fun collectApplicationMetrics() {
        val totalRequests = requestCounter.get()
        val totalErrors = errorCounter.get()
        val activeConn = activeConnections.get()
        val uptime = ManagementFactory.getRuntimeMXBean().uptime

        // 요청 관련 메트릭
        metricsUtils.recordSystemMetric("application.requests.total", totalRequests.toDouble())
        metricsUtils.recordSystemMetric("application.errors.total", totalErrors.toDouble())
        metricsUtils.recordSystemMetric("application.active.connections", activeConn.toDouble())
        metricsUtils.recordSystemMetric("application.uptime.seconds", (uptime / 1000).toDouble())

        // 에러율 계산
        if (totalRequests > 0) {
            val errorRate = (totalErrors.toDouble() / totalRequests) * 100
            metricsUtils.recordSystemMetric("application.error.rate", errorRate)
        }

        // 요청률 계산 (분당)
        val requestsPerMinute = totalRequests / (uptime / 60000.0)
        metricsUtils.recordSystemMetric("application.requests.per.minute", requestsPerMinute)

        StructuredLogging.logInfo(
            logger,
            "Application Metrics Collected",
            "totalRequests" to totalRequests.toString(),
            "totalErrors" to totalErrors.toString(),
            "activeConnections" to activeConn.toString(),
            "uptimeMinutes" to (uptime / 60000).toString(),
            "errorRate" to (if (totalRequests > 0) (totalErrors.toDouble() / totalRequests * 100) else 0.0).toString(),
            "requestsPerMinute" to requestsPerMinute.toString()
        )
    }

    /**
     * 시스템 리소스 메트릭 수집 (매 5분마다)
     */
    @Scheduled(fixedRate = 300000) // 5분
    fun collectSystemResourceMetrics() {
        val availableProcessors = runtime.availableProcessors()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory

        metricsUtils.recordSystemMetric("system.cpu.cores", availableProcessors.toDouble())
        metricsUtils.recordSystemMetric("system.memory.total", totalMemory.toDouble())
        metricsUtils.recordSystemMetric("system.memory.free", freeMemory.toDouble())
        metricsUtils.recordSystemMetric("system.memory.used", usedMemory.toDouble())

        // CPU 사용률 추정 (간단한 방법)
        val cpuLoad = ManagementFactory.getOperatingSystemMXBean().systemLoadAverage
        if (cpuLoad >= 0) {
            metricsUtils.recordSystemMetric("system.cpu.load.average", cpuLoad)
        }

        StructuredLogging.logInfo(
            logger,
            "System Resource Metrics Collected",
            "cpuCores" to availableProcessors.toString(),
            "totalMemoryMB" to (totalMemory / 1024 / 1024).toString(),
            "usedMemoryMB" to (usedMemory / 1024 / 1024).toString(),
            "cpuLoadAverage" to cpuLoad.toString()
        )
    }

    /**
     * 메트릭 카운터 리셋 (테스트용)
     */
    fun resetCounters() {
        requestCounter.set(0)
        errorCounter.set(0)
        activeConnections.set(0)
    }
} 