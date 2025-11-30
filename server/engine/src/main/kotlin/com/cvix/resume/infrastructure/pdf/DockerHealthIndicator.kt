package com.cvix.resume.infrastructure.pdf

import com.github.dockerjava.api.DockerClient
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

private const val UNKNOWN = "unknown"
private const val TEXLIVE_IMAGE_KEY = "texlive.image"

/**
 * Health indicator that checks Docker daemon availability.
 * Used by Spring Boot Actuator to report the health of the PDF generation service.
 *
 * Health check logic:
 * - UP: Docker daemon is accessible and can respond to ping
 * - DOWN: Docker daemon is unreachable or not responding
 *
 * This indicator is critical for the resume generation feature as it relies on
 * Docker to execute LaTeX compilation in isolated containers.
 */
@Component
class DockerHealthIndicator(
    private val dockerClient: DockerClient,
    private val properties: DockerPdfGeneratorProperties,
) : HealthIndicator {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun health(): Health {
        return try {
            // Ping Docker daemon to verify connectivity
            dockerClient.pingCmd().exec()

            // Get Docker version info for diagnostics
            val version = dockerClient.versionCmd().exec()

            // Log successful health check with key Docker info and configured properties.
            // This helps operators observe normal Docker status in logs (debug level).
            logger.debug(
                "Docker health check passed: version={}, apiVersion={}, os={}, arch={}, image={}, " +
                    "timeoutSeconds={}, maxConcurrent={}",
                version.version ?: UNKNOWN,
                version.apiVersion ?: UNKNOWN,
                version.operatingSystem ?: UNKNOWN,
                version.arch ?: UNKNOWN,
                properties.image ?: UNKNOWN,
                properties.timeoutSeconds,
                properties.maxConcurrentContainers,
            )

            Health.up()
                .withDetail("docker.version", version.version ?: UNKNOWN)
                .withDetail("docker.apiVersion", version.apiVersion ?: UNKNOWN)
                .withDetail("docker.os", version.operatingSystem ?: UNKNOWN)
                .withDetail("docker.arch", version.arch ?: UNKNOWN)
                .withDetail(TEXLIVE_IMAGE_KEY, properties.image)
                .withDetail("concurrent.max", properties.maxConcurrentContainers)
                .withDetail("timeout.seconds", properties.timeoutSeconds)
                .build()
        } catch (e: com.github.dockerjava.api.exception.DockerException) {
            logger.error("Docker health check failed", e)
            Health.down()
                .withException(e)
                .withDetail("error", e.message ?: "Docker daemon is not accessible")
                .withDetail(TEXLIVE_IMAGE_KEY, properties.image)
                .build()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.error("Docker health check interrupted", e)
            Health.down()
                .withException(e)
                .withDetail("error", "Health check was interrupted")
                .withDetail(TEXLIVE_IMAGE_KEY, properties.image)
                .build()
        }
    }
}
