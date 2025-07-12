package com.kominioai.global.util

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.DistributionSummary
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.TimeUnit


@Component
class MetricsUtils(private val meterRegistry: MeterRegistry) {

    private val timers = ConcurrentHashMap<String, Timer>()
    private val counters = ConcurrentHashMap<String, Counter>()
    private val gauges = ConcurrentHashMap<String, AtomicLong>()
    private val distributionSummaries = ConcurrentHashMap<String, DistributionSummary>()

    fun startTimer(name: String, tags: Map<String, String> = emptyMap()): Timer.Sample {
        val timer = getOrCreateTimer(name, tags)
        return Timer.start(meterRegistry)
    }

    fun stopTimer(sample: Timer.Sample, name: String, tags: Map<String, String> = emptyMap()) {
        val timer = getOrCreateTimer(name, tags)
        sample.stop(timer)
    }

    private fun getOrCreateTimer(name: String, tags: Map<String, String>): Timer {
        val key = "$name:${tags.hashCode()}"
        return timers.computeIfAbsent(key) {
            Timer.builder(name)
                .tags(tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })
                .register(meterRegistry)
        }
    }

    fun incrementCounter(name: String, tags: Map<String, String> = emptyMap(), amount: Double = 1.0) {
        val counter = getOrCreateCounter(name, tags)
        counter.increment(amount)
    }

    private fun getOrCreateCounter(name: String, tags: Map<String, String>): Counter {
        val key = "$name:${tags.hashCode()}"
        return counters.computeIfAbsent(key) {
            Counter.builder(name)
                .tags(tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })
                .register(meterRegistry)
        }
    }

    fun setGauge(name: String, value: Double, tags: Map<String, String> = emptyMap()) {
        val key = "$name:${tags.hashCode()}"
        val atomicValue = gauges.computeIfAbsent(key) { AtomicLong(0) }
        atomicValue.set(value.toLong())
        
        Gauge.builder(name) { atomicValue.get().toDouble() }
            .tags(tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })
            .register(meterRegistry)
    }

    fun recordDistributionSummary(name: String, value: Double, tags: Map<String, String> = emptyMap()) {
        val summary = getOrCreateDistributionSummary(name, tags)
        summary.record(value)
    }

    private fun getOrCreateDistributionSummary(name: String, tags: Map<String, String>): DistributionSummary {
        val key = "$name:${tags.hashCode()}"
        return distributionSummaries.computeIfAbsent(key) {
            DistributionSummary.builder(name)
                .tags(tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })
                .register(meterRegistry)
        }
    }

    fun recordApiResponseTime(endpoint: String, method: String, statusCode: Int, durationMs: Long) {
        val tags = mapOf(
            "endpoint" to endpoint,
            "method" to method,
            "status" to statusCode.toString()
        )
        
        recordDistributionSummary("api.response.time", durationMs.toDouble(), tags)
        incrementCounter("api.requests.total", tags)
        
        if (statusCode >= 400) {
            incrementCounter("api.errors.total", tags)
        }
    }

    fun recordBusinessMetric(metricName: String, value: Double, tags: Map<String, String> = emptyMap()) {
        recordDistributionSummary("business.$metricName", value, tags)
    }

    fun recordSystemMetric(metricName: String, value: Double, tags: Map<String, String> = emptyMap()) {
        setGauge("system.$metricName", value, tags)
    }
} 