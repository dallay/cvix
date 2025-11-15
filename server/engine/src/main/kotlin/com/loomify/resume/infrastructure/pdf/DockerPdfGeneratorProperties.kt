package com.loomify.resume.infrastructure.pdf

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for Docker PDF generation.
 */
@ConfigurationProperties(prefix = "resume.pdf.docker")
data class DockerPdfGeneratorProperties(
    val image: String = "texlive/texlive:latest",
    val maxConcurrentContainers: Int = 10,
    val timeoutSeconds: Long = 30,
    val memoryLimitMb: Long = 512,
    val cpuQuota: Double = 0.5
)
