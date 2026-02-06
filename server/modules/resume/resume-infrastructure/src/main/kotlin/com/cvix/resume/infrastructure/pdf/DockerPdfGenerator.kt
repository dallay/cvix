package com.cvix.resume.infrastructure.pdf

import com.cvix.resume.domain.PdfGenerator
import com.cvix.resume.domain.exception.LaTeXInjectionException
import com.cvix.resume.domain.exception.PdfGenerationException
import com.cvix.resume.domain.exception.PdfGenerationTimeoutException
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import jakarta.annotation.PostConstruct
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.channels.AsynchronousCloseException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.apache.hc.core5.http.NoHttpResponseException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry

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

    // Track if image has been pulled to avoid redundant pulls
    private val imagePulled = AtomicBoolean(false)

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
    private val containerRetryCounter: Counter = meterRegistry.counter(
        "docker.container.retry",
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
        ) {
            properties.maxConcurrentContainers.toDouble() - concurrencySemaphore.availablePermits()
                .toDouble()
        }

        meterRegistry.gauge(
            "docker.container.concurrent.available",
            this,
        ) { concurrencySemaphore.availablePermits().toDouble() }
    }

    /**
     * Pre-pull the Docker image during application startup to avoid timeouts during the first request.
     * This runs asynchronously to not block application startup.
     */
    @PostConstruct
    fun prePullDockerImage() {
        Schedulers.boundedElastic().schedule {
            try {
                logger.info("Pre-pulling Docker image in background: ${properties.image}")
                ensureDockerImage()
                logger.info("Docker image pre-pull completed successfully: ${properties.image}")
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                logger.warn(
                    "Failed to pre-pull Docker image during startup. " +
                        "Image will be pulled on first request: ${e.message}",
                    e,
                )
            }
        }
    }

    override fun generatePdf(latexSource: String, locale: String): Mono<InputStream> {
        return Mono.fromCallable { acquireAndRunPdfGeneration(latexSource, locale) }
            .subscribeOn(Schedulers.boundedElastic())
            .retryWhen(
                Retry.backoff(MAX_RETRIES, Duration.ofMillis(RETRY_INITIAL_BACKOFF_MS))
                    .maxBackoff(Duration.ofSeconds(RETRY_MAX_BACKOFF_SECONDS))
                    .filter { error -> isRetryableError(error) }
                    .doBeforeRetry { signal ->
                        containerRetryCounter.increment()
                        val attempt = signal.totalRetries() + 1
                        val errorMsg = signal.failure().message
                        logger.warn(
                            "Retrying PDF generation after transient error " +
                                "(attempt $attempt/$MAX_RETRIES): $errorMsg",
                        )
                    },
            )
            .timeout(Duration.ofSeconds(properties.timeoutSeconds + TIMEOUT_BUFFER_SECONDS))
            .onErrorMap(this::mapPdfGenerationError)
    }

    /**
     * Determines if an error is transient and should trigger a retry.
     * Retryable errors include:
     * - NoHttpResponseException: Docker socket proxy didn't respond (connection issues)
     * - Connection reset/refused errors
     * - AsynchronousCloseException: Connection closed during operation
     */
    private fun isRetryableError(error: Throwable): Boolean {
        val rootCause = getRootCause(error)
        return when {
            // NoHttpResponseException from Apache HttpClient5 - proxy didn't respond
            rootCause is NoHttpResponseException -> true
            // Connection was closed during operation
            rootCause is AsynchronousCloseException -> true
            // Connection reset by peer
            rootCause is java.net.SocketException &&
                rootCause.message?.contains("reset", ignoreCase = true) == true -> true
            // Connection refused
            rootCause is java.net.ConnectException -> true
            // Check message for common transient patterns
            rootCause.message?.let { msg ->
                msg.contains("failed to respond", ignoreCase = true) ||
                    msg.contains("connection reset", ignoreCase = true) ||
                    msg.contains("broken pipe", ignoreCase = true)
            } == true -> true

            else -> false
        }
    }

    private fun getRootCause(error: Throwable): Throwable {
        var cause = error
        var next = cause.cause
        while (next != null && next != cause) {
            cause = next
            next = cause.cause
        }
        return cause
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

        var containerId: String
        try {
            // Write LaTeX source to file
            val latexFile = tempDir.resolve(LATEX_FILE)
            Files.writeString(latexFile, latexSource)

            // Pull Docker image if not present (async, first time only)
            ensureDockerImage()

            // Create and run Docker container
            containerId = createContainer(tempDir)

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
            } catch (e: AsynchronousCloseException) {
                logger.error("Docker connection closed prematurely during PDF generation", e)
                throw PdfGenerationException(
                    "Docker connection closed prematurely. " +
                        "Consider increasing Docker resource limits or debugging container lifecycle.",
                    e,
                )
            } finally {
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
        // Fast path: if image has been verified, skip the check
        if (imagePulled.get()) {
            return
        }

        // Check if image exists
        try {
            dockerClient.inspectImageCmd(properties.image).exec()
            logger.debug("Docker image already available: ${properties.image}")
            imagePulled.set(true)
            return
        } catch (_: NotFoundException) {
            // Image not found locally, pull it
        }

        // Pull the image (synchronized to prevent multiple threads from pulling simultaneously)
        synchronized(imagePulled) {
            // Double-check in case another thread just pulled it
            try {
                dockerClient.inspectImageCmd(properties.image).exec()
                logger.debug("Docker image verified (pulled by another thread): ${properties.image}")
                imagePulled.set(true)
                return
            } catch (_: NotFoundException) {
                // Still not available, proceed with pull
            }

            try {
                logger.info("Pulling Docker image: ${properties.image} (this may take several minutes)")
                dockerClient.pullImageCmd(properties.image)
                    .start()
                    .awaitCompletion(PULL_TIMEOUT_MIN, TimeUnit.MINUTES)
                logger.info("Docker image pull completed: ${properties.image}")
                imagePulled.set(true)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
                throw PdfGenerationException("Interrupted while pulling Docker image", ie)
            } catch (@Suppress("TooGenericExceptionCaught") e: RuntimeException) {
                handleAsyncException(e)
            } catch (de: com.github.dockerjava.api.exception.DockerException) {
                throw PdfGenerationException("Failed to pull Docker image: ${properties.image}", de)
            }
        }
    }

    private fun handleAsyncException(e: RuntimeException) {
        // Handle AsynchronousCloseException wrapped in RuntimeException during image pull
        val cause = e.cause
        if (cause is AsynchronousCloseException) {
            logger.warn("Docker connection closed during image pull, retrying inspection", e)
            // Retry inspection - image might have been pulled by another thread
            try {
                dockerClient.inspectImageCmd(properties.image).exec()
                logger.info("Docker image verified after interrupted pull: ${properties.image}")
                imagePulled.set(true)
                return
            } catch (_: NotFoundException) {
                throw PdfGenerationException(
                    "Docker image pull was interrupted and image is not available: ${properties.image}",
                    e,
                )
            } catch (poolShutdown: IllegalStateException) {
                // Connection pool was shut down during timeout - cannot verify image
                logger.error(
                    "Docker connection pool shut down during image pull verification: ${poolShutdown.message}",
                    poolShutdown,
                )
                throw PdfGenerationException(
                    "Docker image pull was interrupted and connection pool shut down. " +
                        "The image may need to be pre-pulled or timeouts increased: ${properties.image}",
                    e,
                )
            }
        }
        throw PdfGenerationException("Failed to pull Docker image: ${properties.image}", e)
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
            .withUser(properties.containerUser) // Run as same UID as backend for file access
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
                logger.debug(
                    "Failed to stop container (might already be stopped): $containerId",
                    de,
                )
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
        private const val PULL_TIMEOUT_MIN = 5L // Increased to 5 minutes for slow CI
        private const val TIMEOUT_BUFFER_SECONDS =
            60L // Increased to 60s to accommodate Docker image pulls in CI
        private const val SEMAPHORE_TIMEOUT_BUFFER = 60L // Increased to 60s
        private const val STOP_TIMEOUT_SECONDS = 5
        private const val UNABLE_TO_RETRIEVE_LOGS = "Unable to retrieve logs"

        // Retry configuration for transient Docker socket proxy errors
        private const val MAX_RETRIES = 3L
        private const val RETRY_INITIAL_BACKOFF_MS = 500L // Start with 500ms backoff
        private const val RETRY_MAX_BACKOFF_SECONDS = 5L // Max 5s between retries
    }
}
