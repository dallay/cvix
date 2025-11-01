package com.loomify.resume.infrastructure.pdf

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val MAX_CONNECTIONS = 100

private const val CONNECTION_TIMEOUT = 30

private const val RESPONSE_TIMEOUT = 15

/**
 * Docker configuration for PDF generation.
 * Configures Docker client and resource limits per security requirements.
 */
@Configuration
class DockerConfiguration {

    @Bean
    fun dockerClient(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build()

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(MAX_CONNECTIONS)
            .connectionTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT))
            .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT))
            .build()

        return DockerClientImpl.getInstance(config, httpClient)
    }

    @Bean
    fun dockerPdfGeneratorProperties(): DockerPdfGeneratorProperties =
        DockerPdfGeneratorProperties()
}

/**
 * Configuration properties for Docker PDF generator.
 */
@ConfigurationProperties(prefix = "resume.pdf.docker")
data class DockerPdfGeneratorProperties(
    /**
     * TeX Live Docker image to use for PDF generation.
     * Default: texlive/texlive:latest-minimal
     */
    var image: String = "texlive/texlive:latest-minimal",

    /**
     * Maximum number of concurrent Docker containers.
     * Default: 10 (per spec.md - supports 50 concurrent users)
     */
    var maxConcurrentContainers: Int = 10,

    /**
     * Timeout for PDF generation in seconds.
     * Default: 10 seconds (per plan.md)
     */
    var timeoutSeconds: Long = 10,

    /**
     * Memory limit for Docker containers in MB.
     * Default: 512MB (per plan.md)
     */
    var memoryLimitMb: Long = 512,

    /**
     * CPU quota for Docker containers (fraction of 1 CPU core).
     * Default: 0.5 CPU (per plan.md)
     */
    var cpuQuota: Double = 0.5
)
