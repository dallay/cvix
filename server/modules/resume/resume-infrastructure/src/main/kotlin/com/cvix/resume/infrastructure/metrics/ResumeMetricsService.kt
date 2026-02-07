package com.cvix.resume.infrastructure.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component

/**
 * Metrics service for tracking Resume Generator API performance and SLOs.
 *
 * Key Performance Indicators:
 * - API p95 latency: Target ≤200ms (excluding PDF generation)
 * - PDF generation: Target <8 seconds
 * - Error rate: Target <3%
 * - Uptime: Target 99.5%
 */
@Component
@Suppress("TooManyFunctions") // Justified: Each function tracks a specific metric type
class ResumeMetricsService(
    meterRegistry: MeterRegistry,
) {

    // API latency timer (excludes PDF generation time)
    private val apiLatencyTimer: Timer = Timer.builder("resume.api.latency")
        .description("API endpoint latency excluding PDF generation")
        .publishPercentiles(0.5, 0.75, 0.95, 0.99)
        .publishPercentileHistogram()
        .serviceLevelObjectives(
            java.time.Duration.ofMillis(50),
            java.time.Duration.ofMillis(100),
            java.time.Duration.ofMillis(200), // Target SLO
            java.time.Duration.ofMillis(250), // Critical threshold
            java.time.Duration.ofMillis(500),
        )
        .register(meterRegistry)

    // Request counters
    private val requestTotalCounter: Counter = Counter.builder("resume.requests.total")
        .description("Total resume generation requests")
        .register(meterRegistry)

    private val requestSuccessCounter: Counter = Counter.builder("resume.requests.success")
        .description("Successful resume generation requests")
        .register(meterRegistry)

    private val requestFailureCounter: Counter = Counter.builder("resume.requests.failure")
        .description("Failed resume generation requests")
        .register(meterRegistry)

    private val timeoutErrorCounter: Counter = Counter.builder("resume.errors.timeout")
        .description("Timeout errors")
        .register(meterRegistry)

    private val rateLimitErrorCounter: Counter = Counter.builder("resume.errors.ratelimit")
        .description("Rate limit errors")
        .register(meterRegistry)

    init {
        // Register derived error rate gauge (failures / total)
        meterRegistry.gauge(
            "resume.error.rate",
            this,
        ) {
            val total = requestTotalCounter.count()
            val failures = requestFailureCounter.count()
            if (total > 0) failures / total else 0.0
        }

        // Register initialized gauge to indicate if requests have been recorded
        meterRegistry.gauge(
            "resume.error.rate.initialized",
            this,
        ) {
            if (requestTotalCounter.count() > 0) 1.0 else 0.0
        }
    }

    /**
     * Record API latency (excluding PDF generation)
     */
    fun recordApiLatency(durationMs: Long) {
        apiLatencyTimer.record(durationMs, TimeUnit.MILLISECONDS)
    }

    /**
     * Increment total request counter
     */
    fun incrementRequestTotal() {
        requestTotalCounter.increment()
    }

    /**
     * Increment successful request counter
     */
    fun incrementRequestSuccess() {
        requestSuccessCounter.increment()
    }

    /**
     * Increment failure counter
     */
    fun incrementRequestFailure() {
        requestFailureCounter.increment()
    }

    /**
     * Increment timeout error counter
     */
    fun incrementTimeoutError() {
        timeoutErrorCounter.increment()
    }

    /**
     * Increment rate limit error counter
     */
    fun incrementRateLimitError() {
        rateLimitErrorCounter.increment()
    }
}
