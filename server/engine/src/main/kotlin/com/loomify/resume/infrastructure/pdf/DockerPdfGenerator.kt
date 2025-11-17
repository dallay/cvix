package com.loomify.resume.infrastructure.pdf

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.PdfGenerationException
import com.loomify.resume.domain.exception.PdfGenerationTimeoutException
import com.loomify.resume.domain.PdfGenerator
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Adapter that generates PDFs from LaTeX source using Docker containers.
 * Implements strict security measures including container isolation and resource limits.
 * Uses a semaphore to limit concurrent Docker container executions.
 */
@Component
class DockerPdfGenerator(
    private val dockerClient: DockerClient,
    private val properties: DockerPdfGeneratorProperties,
    meterRegistry: MeterRegistry,
) : PdfGenerator {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Semaphore to limit concurrent container executions
    private val concurrencySemaphore = Semaphore(properties.maxConcurrentContainers)

    // Metrics
    private val containerCreatedCounter: Counter = meterRegistry.counter(
        "docker.container.created",
        METRICS_TAG, METRICS_COMPONENT,
    )
    private val containerStartedCounter: Counter = meterRegistry.counter(
        "docker.container.started",
        METRICS_TAG, METRICS_COMPONENT,
    )
    private val containerCompletedCounter: Counter = meterRegistry.counter(
        "docker.container.completed",
        METRICS_TAG, METRICS_COMPONENT,
    )
    private val containerFailedCounter: Counter = meterRegistry.counter(
        "docker.container.failed",
        METRICS_TAG, METRICS_COMPONENT,
    )
    private val containerTimeoutCounter: Counter = meterRegistry.counter(
        "docker.container.timeout",
        METRICS_TAG, METRICS_COMPONENT,
    )
    private val containerCleanupCounter: Counter = meterRegistry.counter(
        "docker.container.cleanup",
        METRICS_TAG, METRICS_COMPONENT,
    )
    private val containerExecutionTimer: Timer = meterRegistry.timer(
        "docker.container.execution.duration",
        METRICS_TAG, METRICS_COMPONENT,
    )

    init {
        // Register gauge for concurrent container usage
        meterRegistry.gauge(
            "docker.container.concurrent.active",
            this,
        ) { properties.maxConcurrentContainers - concurrencySemaphore.availablePermits().toDouble() }

        meterRegistry.gauge(
            "docker.container.concurrent.available",
            this,
        ) { concurrencySemaphore.availablePermits().toDouble() }
    }

    override fun generatePdf(latexSource: String, locale: String): Mono<InputStream> {
        return Mono.fromCallable { acquireAndRunPdfGeneration(latexSource, locale) }
            .subscribeOn(Schedulers.boundedElastic())
            .timeout(Duration.ofSeconds(properties.timeoutSeconds + TIMEOUT_BUFFER_SECONDS))
            .onErrorMap(this::mapPdfGenerationError)
    }

    private fun acquireAndRunPdfGeneration(latexSource: String, locale: String): InputStream {
        // Acquire semaphore permit (blocks if max concurrent containers reached)
        val acquired = concurrencySemaphore.tryAcquire(
            properties.timeoutSeconds + SEMAPHORE_TIMEOUT_BUFFER,
            TimeUnit.SECONDS,
        )

        if (!acquired) {
            val msg = "Failed to acquire container slot: maximum concurrent containers " +
                "(${properties.maxConcurrentContainers}) reached"
            throw PdfGenerationException(msg)
        }

        val usedPermits = properties.maxConcurrentContainers -
            concurrencySemaphore.availablePermits()
        logger.debug(
            "Acquired container slot ($usedPermits/${properties.maxConcurrentContainers} in use)",
        )

        try {
            return generatePdfInContainer(latexSource, locale)
        } finally {
            // Release semaphore permit
            concurrencySemaphore.release()
            val inUse = properties.maxConcurrentContainers -
                concurrencySemaphore.availablePermits()
            logger.debug(
                "Released container slot ($inUse/${properties.maxConcurrentContainers} in use)",
            )
        }
    }

    private fun generatePdfInContainer(latexSource: String, locale: String): InputStream {
        // Note: Security validation of user content is performed in LatexTemplateRenderer
        // before rendering. The latexSource here is the rendered template which contains
        // safe LaTeX commands that would trigger false positives.
        logger.debug("Starting PDF generation for locale: $locale")

        // Create temporary directory for LaTeX compilation
        val tempDir = Files.createTempDirectory("resume-pdf-")

        try {
            // Write LaTeX source to file
            val latexFile = tempDir.resolve(LATEX_FILE)
            Files.writeString(latexFile, latexSource)

            // Pull Docker image if not present (async, first time only)
            ensureDockerImage()

            // Create and run Docker container
            val containerId = createContainer(tempDir)

            try {
                // Start container
                dockerClient.startContainerCmd(containerId).exec()

                // Wait for container to complete with timeout
                val exitCode = waitForContainer(containerId)

                if (exitCode != 0L) {
                    val logs = getContainerLogs(containerId)
                    val errMsg = "LaTeX compilation failed with exit code $exitCode. " +
                        "Logs: $logs"
                    throw PdfGenerationException(errMsg)
                }

                // Read generated PDF
                val pdfFile = tempDir.resolve(PDF_FILE)
                if (!Files.exists(pdfFile)) {
                    throw PdfGenerationException("PDF file was not generated")
                }

                val pdfBytes = Files.readAllBytes(pdfFile)
                logger.debug("Successfully generated PDF (${pdfBytes.size} bytes)")

                return ByteArrayInputStream(pdfBytes)
            } finally {
                // Cleanup: Remove container
                cleanupContainer(containerId)
            }
        } finally {
            // Cleanup: Delete temporary directory
            tempDir.toFile().deleteRecursively()
        }
    }

    private fun mapPdfGenerationError(error: Throwable): Throwable {
        val pdfGenerationTimeoutException = PdfGenerationTimeoutException(
            "PDF generation timed out after ${properties.timeoutSeconds} seconds",
            error,
        )
        return when {
            error is PdfGenerationTimeoutException -> error
            error is PdfGenerationException -> error
            error is LaTeXInjectionException -> error
            error is java.util.concurrent.TimeoutException ->
                pdfGenerationTimeoutException
            error.message?.contains("timeout", ignoreCase = true) == true ->
                pdfGenerationTimeoutException
            else -> {
                val msg = "Failed to generate PDF: ${error.message}"
                PdfGenerationException(msg, error)
            }
        }
    }

    private fun ensureDockerImage() {
        try {
            dockerClient.inspectImageCmd(properties.image).exec()
            logger.debug("Docker image already available: ${properties.image}")
        } catch (_: com.github.dockerjava.api.exception.NotFoundException) {
            // Image not found locally, pull it
            try {
                logger.info("Pulling Docker image: ${properties.image}")
                dockerClient.pullImageCmd(properties.image)
                    .start()
                    .awaitCompletion(PULL_TIMEOUT_MIN, TimeUnit.MINUTES)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
                throw PdfGenerationException("Interrupted while pulling Docker image", ie)
            } catch (de: com.github.dockerjava.api.exception.DockerException) {
                throw PdfGenerationException("Failed to pull Docker image: ${properties.image}", de)
            }
        }
    }

    private fun createContainer(tempDir: Path): String {
        val hostConfig = HostConfig.newHostConfig()
            .withBinds(Bind(tempDir.toString(), Volume(WORK_DIR)))
            .withReadonlyRootfs(true) // Security: read-only filesystem
            .withMemory(properties.memoryLimitMb * MB * MB) // Memory limit
            .withCpuQuota((properties.cpuQuota * CPU_QUOTA_UNIT).toLong()) // CPU limit
            .withCpuPeriod(CPU_QUOTA_UNIT)
            .withNetworkMode("none") // Security: no network access
            .withAutoRemove(false) // We handle cleanup manually
        val response: CreateContainerResponse = dockerClient.createContainerCmd(properties.image)
            .withWorkingDir(WORK_DIR)
            .withCmd("pdflatex", "-interaction=nonstopmode", "-halt-on-error", LATEX_FILE)
            .withHostConfig(hostConfig)
            .withTty(false)
            .withAttachStdout(true)
            .withAttachStderr(true)
            .exec()

        containerCreatedCounter.increment()
        logger.debug("Created Docker container: ${response.id}")
        return response.id
    }

    private fun waitForContainer(containerId: String): Long {
        val startTime = System.currentTimeMillis()
        val timeoutMillis = properties.timeoutSeconds * SECOND

        containerStartedCounter.increment()

        try {
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                checkTimeout(elapsed, timeoutMillis)

                val info = dockerClient.inspectContainerCmd(containerId).exec()
                val state = info.state

                if (state.running == false) {
                    return handleContainerExit(state.exitCodeLong ?: -1L, elapsed)
                }

                Thread.sleep(POLL_INTERVAL_MS) // Poll every 100ms
            }
        } catch (e: PdfGenerationTimeoutException) {
            // Re-throw timeout exceptions after tracking
            throw e
        } catch (e: com.github.dockerjava.api.exception.DockerException) {
            containerFailedCounter.increment()
            throw e
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            containerFailedCounter.increment()
            throw e
        }
    }

    private fun checkTimeout(elapsed: Long, timeoutMillis: Long) {
        if (elapsed > timeoutMillis) {
            containerTimeoutCounter.increment()
            throw PdfGenerationTimeoutException(
                "Container execution timed out after ${properties.timeoutSeconds} seconds",
            )
        }
    }

    private fun handleContainerExit(exitCode: Long, elapsed: Long): Long {
        if (exitCode == 0L) {
            containerCompletedCounter.increment()
            // Record execution time
            containerExecutionTimer.record(elapsed, TimeUnit.MILLISECONDS)
        } else {
            containerFailedCounter.increment()
        }
        return exitCode
    }

    private fun getContainerLogs(containerId: String): String {
        return try {
            val logs = StringBuilder()
            val callback = object :
                com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {
                override fun onNext(frame: com.github.dockerjava.api.model.Frame) {
                    logs.append(String(frame.payload))
                }
            }

            dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .exec(callback)
                .awaitCompletion(LOG_TIMEOUT_SEC, TimeUnit.SECONDS)

            logs.toString()
        } catch (ie: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.warn("Interrupted while retrieving container logs for $containerId", ie)
            UNABLE_TO_RETRIEVE_LOGS
        } catch (de: com.github.dockerjava.api.exception.DockerException) {
            logger.warn("Failed to retrieve container logs due to Docker exception", de)
            UNABLE_TO_RETRIEVE_LOGS
        }
    }

    private fun cleanupContainer(containerId: String) {
        try {
            // Stop container if still running
            try {
                dockerClient.stopContainerCmd(containerId)
                    .withTimeout(STOP_TIMEOUT_SECONDS)
                    .exec()
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
                logger.debug("Interrupted while stopping container: $containerId", ie)
            } catch (de: com.github.dockerjava.api.exception.DockerException) {
                logger.debug("Failed to stop container (might already be stopped): $containerId", de)
            }

            // Remove container
            try {
                dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec()
                containerCleanupCounter.increment()
            } catch (de: com.github.dockerjava.api.exception.DockerException) {
                logger.debug("Failed to remove container: $containerId", de)
            }

            logger.debug("Cleaned up Docker container: $containerId")
        } catch (ie: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.warn("Interrupted while cleaning up container: $containerId", ie)
        } catch (de: com.github.dockerjava.api.exception.DockerException) {
            logger.warn("Failed to cleanup container due to Docker exception: $containerId", de)
        }
    }

    companion object {
        private const val METRICS_COMPONENT = "pdf-generator"
        private const val METRICS_TAG = "component"
        private const val WORK_DIR = "/work"
        private const val LATEX_FILE = "resume.tex"
        private const val PDF_FILE = "resume.pdf"
        private const val MB = 1_024L
        private const val CPU_QUOTA_UNIT = 100_000L
        private const val SECOND = 1_000
        private const val POLL_INTERVAL_MS = 100L
        private const val LOG_TIMEOUT_SEC = 5L
        private const val PULL_TIMEOUT_MIN = 2L
        private const val TIMEOUT_BUFFER_SECONDS = 5L
        private const val SEMAPHORE_TIMEOUT_BUFFER = 10L
        private const val STOP_TIMEOUT_SECONDS = 5
        private const val UNABLE_TO_RETRIEVE_LOGS = "Unable to retrieve logs"
    }
}
