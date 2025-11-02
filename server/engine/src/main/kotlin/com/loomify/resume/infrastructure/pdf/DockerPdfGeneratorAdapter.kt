package com.loomify.resume.infrastructure.pdf

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.PdfGenerationException
import com.loomify.resume.domain.exception.PdfGenerationTimeoutException
import com.loomify.resume.domain.port.PdfGeneratorPort
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Adapter that generates PDFs from LaTeX source using Docker containers.
 * Implements strict security measures including container isolation and resource limits.
 */
@Component
class DockerPdfGeneratorAdapter(
    private val dockerClient: DockerClient,
    private val properties: DockerPdfGeneratorProperties
) : PdfGeneratorPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun generatePdf(latexSource: String, locale: String): Mono<InputStream> {
        return Mono.fromCallable<InputStream> {
            // Security check: Validate LaTeX source
            validateLatexSource(latexSource)

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
                        throw PdfGenerationException(
                            "LaTeX compilation failed with exit code $exitCode. Logs: $logs",
                        )
                    }

                    // Read generated PDF
                    val pdfFile = tempDir.resolve(PDF_FILE)
                    if (!Files.exists(pdfFile)) {
                        throw PdfGenerationException("PDF file was not generated")
                    }

                    val pdfBytes = Files.readAllBytes(pdfFile)
                    logger.debug("Successfully generated PDF (${pdfBytes.size} bytes)")

                    ByteArrayInputStream(pdfBytes)
                } finally {
                    // Cleanup: Remove container
                    cleanupContainer(containerId)
                }
            } finally {
                // Cleanup: Delete temporary directory
                tempDir.toFile().deleteRecursively()
            }
        }
            .subscribeOn(Schedulers.boundedElastic())
            .timeout(Duration.ofSeconds(properties.timeoutSeconds + TIMEOUT_BUFFER_SECONDS))
            .onErrorMap { error ->
                when {
                    error is PdfGenerationException -> error
                    error is LaTeXInjectionException -> error
                    error.message?.contains("timeout", ignoreCase = true) == true ->
                        PdfGenerationTimeoutException(
                            "PDF generation timed out after ${properties.timeoutSeconds} seconds",
                        )
                    else -> {
                        val msg = "Failed to generate PDF: ${error.message}"
                        PdfGenerationException(msg, error)
                    }
                }
            }
    }

    private fun validateLatexSource(latexSource: String) {
        // Check for potentially malicious commands
        val dangerousPatterns = listOf(
            """\\input{""",
            """\\include{""",
            """\\write""",
            """\\openin""",
            """\\openout""",
        )

        dangerousPatterns.forEach { pattern ->
            if (latexSource.contains(pattern, ignoreCase = true)) {
                throw LaTeXInjectionException(
                    "Potentially malicious LaTeX command detected in source",
                )
            }
        }
    }

    private fun ensureDockerImage() {
        try {
            dockerClient.inspectImageCmd(properties.image).exec()
            logger.debug("Docker image already available: ${properties.image}")
        } catch (e: com.github.dockerjava.api.exception.NotFoundException) {
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
        logger.debug("Created Docker container: ${response.id}")
        return response.id
    }

    private fun waitForContainer(containerId: String): Long {
        val startTime = System.currentTimeMillis()
        val timeoutMillis = properties.timeoutSeconds * SECOND
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed > timeoutMillis) {
                throw PdfGenerationTimeoutException(
                    "Container execution timed out after ${properties.timeoutSeconds} seconds",
                )
            }
            val info = dockerClient.inspectContainerCmd(containerId).exec()
            val state = info.state
            if (state.running == false) {
                return state.exitCodeLong ?: -1L
            }
            Thread.sleep(POLL_INTERVAL_MS) // Poll every 100ms
        }
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
        private const val WORK_DIR = "/work"
        private const val LATEX_FILE = "resume.tex"
        private const val PDF_FILE = "resume.pdf"
        private const val MB = 1_024
        private const val CPU_QUOTA_UNIT = 100_000L
        private const val SECOND = 1_000
        private const val POLL_INTERVAL_MS = 100L
        private const val LOG_TIMEOUT_SEC = 5L
        private const val PULL_TIMEOUT_MIN = 2L
        private const val TIMEOUT_BUFFER_SECONDS = 5L
        private const val STOP_TIMEOUT_SECONDS = 5
        private const val UNABLE_TO_RETRIEVE_LOGS = "Unable to retrieve logs"
    }
}
