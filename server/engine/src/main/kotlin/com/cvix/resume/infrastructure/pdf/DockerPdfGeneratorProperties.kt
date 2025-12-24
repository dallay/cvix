package com.cvix.resume.infrastructure.pdf

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for Docker PDF generation.
 */
@ConfigurationProperties(prefix = "resume.pdf.docker")
data class DockerPdfGeneratorProperties(
    /** Docker image for TexLive. Use ghcr.io/cvix/texlive:2024 for multi-arch support (amd64/arm64). */
    val image: String = "ghcr.io/cvix/texlive:2024",
    val maxConcurrentContainers: Int = 10,
    val timeoutSeconds: Long = 60, // Increased to 60s for CI environments
    val memoryLimitMb: Long = 512,
    val cpuQuota: Double = 0.5
)
