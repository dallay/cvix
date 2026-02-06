package com.cvix.identity.infrastructure.workspace.metrics

import com.cvix.identity.domain.workspace.WorkspaceMetrics
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

/**
 * Micrometer-based implementation of [WorkspaceMetrics].
 * This is the infrastructure adapter that bridges the domain abstraction
 * to the Micrometer metrics library.
 *
 * @property meterRegistry The Micrometer registry for creating and managing metrics.
 */
@Component
class MicrometerWorkspaceMetrics(
    meterRegistry: MeterRegistry,
) : WorkspaceMetrics {

    private val dupDefaultWsIgnoredCounter: Counter = Counter
        .builder(METRIC_WS_DEFAULT_DUP_IGN)
        .description("Count of ignored duplicate default workspace creations")
        .register(meterRegistry)

    override fun incrementDuplicateDefaultIgnored() {
        dupDefaultWsIgnoredCounter.increment()
    }

    companion object {
        private const val METRIC_WS_DEFAULT_DUP_IGN = "workspace.default.duplicate.ignored"
    }
}
