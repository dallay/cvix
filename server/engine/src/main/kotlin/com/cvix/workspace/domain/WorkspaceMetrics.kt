package com.cvix.workspace.domain

/**
 * Domain-level abstraction for workspace metrics.
 * This allows the application layer to track metrics without depending on infrastructure (Micrometer).
 *
 * Following hexagonal architecture principles:
 * - Domain defines the contract (this interface)
 * - Infrastructure provides the implementation (e.g., MicrometerWorkspaceMetrics)
 * - Application layer depends only on the domain abstraction
 */
interface WorkspaceMetrics {
    /**
     * Increments the counter for ignored duplicate default workspace creations.
     * This metric tracks how often we prevent duplicate default workspace creation
     * due to race conditions.
     */
    fun incrementDuplicateDefaultIgnored()
}
