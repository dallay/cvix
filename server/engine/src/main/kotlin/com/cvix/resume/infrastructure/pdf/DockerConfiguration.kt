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

private const val MAX_CONNECTIONS = 100

/**
 * Docker configuration for PDF generation.
 * Configures Docker client and resource limits per security requirements.
 */
@Configuration
@EnableConfigurationProperties(DockerPdfGeneratorProperties::class)
class DockerConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun dockerClient(properties: DockerPdfGeneratorProperties): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build()

        logger.info("Initializing Docker client with host: ${config.dockerHost}")
        logger.info("DOCKER_HOST env: ${System.getenv("DOCKER_HOST") ?: "not set"}")

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(MAX_CONNECTIONS)
            .connectionTimeout(Duration.ofSeconds(properties.timeoutSeconds))
            .responseTimeout(Duration.ofSeconds(properties.timeoutSeconds))
            .build()

        val client = DockerClientImpl.getInstance(config, httpClient)

        // Verify Docker connection on startup
        try {
            val version = client.versionCmd().exec()
            logger.info("Docker connection successful. Version: ${version.version}, API: ${version.apiVersion}")
        } catch (e: com.github.dockerjava.api.exception.DockerException) {
            logger.error("Failed to connect to Docker daemon at ${config.dockerHost}: ${e.message}", e)
        } catch (@Suppress("TooGenericExceptionCaught") e: RuntimeException) {
            // RuntimeException is intentionally caught here to log ANY Docker connection failure
            // (including transport errors, timeouts, etc.) without crashing the application.
            logger.error("Docker connection error at ${config.dockerHost}: ${e.message}", e)
        }

        return client
    }
}
