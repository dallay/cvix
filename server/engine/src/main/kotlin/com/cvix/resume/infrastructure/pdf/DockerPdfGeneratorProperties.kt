package com.cvix.resume.infrastructure.pdf

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for Docker PDF generation.
 */
@ConfigurationProperties(prefix = "resume.pdf.docker")
data class DockerPdfGeneratorProperties(
    val image: String = "texlive/texlive:TL2024-historic",
    val maxConcurrentContainers: Int = 10,
    val timeoutSeconds: Long = 60, // Increased to 60s for CI environments
    val memoryLimitMb: Long = 512,
    val cpuQuota: Double = 0.5
)
