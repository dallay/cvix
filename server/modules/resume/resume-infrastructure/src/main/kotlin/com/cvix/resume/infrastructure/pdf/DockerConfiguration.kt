package com.cvix.resume.infrastructure.pdf

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Maximum number of concurrent HTTP connections to Docker daemon.
 * This should be >= maxConcurrentContainers to avoid connection pool exhaustion.
 */
private const val MAX_CONNECTIONS = 100

/**
 * Connection timeout for establishing new connections to Docker daemon.
 * Keep this short - if the proxy is down, we want to fail fast.
 */
private const val CONNECTION_TIMEOUT_SECONDS = 10L

/**
 * Docker configuration for PDF generation.
 * Configures Docker client and resource limits per security requirements.
 *
 * Note: Connection stability with docker-socket-proxy is handled via:
 * 1. Short connection timeout to fail fast on dead connections
 * 2. Response timeout matching the PDF generation timeout
 * 3. Retry logic in DockerPdfGenerator for transient failures
 */
@Configuration
@EnableConfigurationProperties(DockerPdfGeneratorProperties::class)
class DockerConfiguration {

    private val logger = LoggerFactory.getLogger(DockerConfiguration::class.java)

    /**
     * Creates and configures a DockerClient used for PDF generation.
     *
     * Builds the Docker client configuration and HTTP transport using the provided properties, initializes
     * the DockerClient instance, and attempts a startup verification against the Docker daemon, logging
     * success or failure without preventing application startup.
     *
     * @param properties Configuration values for the PDF generator (used for timeouts and related settings).
     * @return A configured DockerClient instance ready for use by PDF generation components.
     */
    @Bean
    fun dockerClient(properties: DockerPdfGeneratorProperties): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build()

        logger.info("Initializing Docker client with host: {}", config.dockerHost)
        logger.info("DOCKER_HOST env: {}", System.getenv("DOCKER_HOST") ?: "not set")

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(MAX_CONNECTIONS)
            // Short connection timeout - fail fast if proxy is unresponsive
            .connectionTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
            // Response timeout should match PDF generation timeout
            .responseTimeout(Duration.ofSeconds(properties.timeoutSeconds))
            .build()

        val client = DockerClientImpl.getInstance(config, httpClient)

        // Verify Docker connection on startup
        try {
            val version = client.versionCmd().exec()
            logger.info("Docker connection successful. Version: {}, API: {}", version.version, version.apiVersion)
        } catch (e: com.github.dockerjava.api.exception.DockerException) {
            logger.error("Failed to connect to Docker daemon at {}: {}", config.dockerHost, e.message, e)
        } catch (@Suppress("TooGenericExceptionCaught") e: RuntimeException) {
            // RuntimeException is intentionally caught here to log ANY Docker connection failure
            // (including transport errors, timeouts, etc.) without crashing the application.
            logger.error("Docker connection error at {}: {}", config.dockerHost, e.message)
        }

        return client
    }
}
