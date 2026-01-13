package com.cvix.resume.infrastructure.pdf

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for Docker PDF generation.
 */
@ConfigurationProperties(prefix = "resume.pdf.docker")
data class DockerPdfGeneratorProperties(
    /** Docker image for TexLive. Use ghcr.io/dallay/texlive:2025 for multi-arch support (amd64/arm64). */
    val image: String = "ghcr.io/dallay/texlive:2025",
    val maxConcurrentContainers: Int = 10,
    val timeoutSeconds: Long = 60, // Increased to 60s for CI environments
    val memoryLimitMb: Long = 512,
    val cpuQuota: Double = 0.5,
    /**
     * User ID for running TexLive container.
     * Must match the backend process UID so the container can access temp files
     * without requiring world-readable permissions (security best practice).
     * Default "999" matches the 'spring' user created in the backend Dockerfile.
     * Override via RESUME_PDF_DOCKER_CONTAINER_USER env var if your setup differs.
     */
    val containerUser: String = "999"
)
