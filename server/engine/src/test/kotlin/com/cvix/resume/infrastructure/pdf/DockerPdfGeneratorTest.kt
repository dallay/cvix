package com.cvix.resume.infrastructure.pdf

import com.cvix.UnitTest
import com.cvix.resume.domain.exception.PdfGenerationException
import com.cvix.resume.domain.exception.PdfGenerationTimeoutException
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.InspectContainerCmd
import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.api.command.InspectImageCmd
import com.github.dockerjava.api.command.RemoveContainerCmd
import com.github.dockerjava.api.command.StartContainerCmd
import com.github.dockerjava.api.command.StopContainerCmd
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import org.apache.hc.core5.http.NoHttpResponseException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Unit test for DockerPdfGeneratorAdapter.
 * Tests PDF generation, security validation, and Docker interaction.
 */
@UnitTest
class DockerPdfGeneratorTest {

    private lateinit var dockerClient: DockerClient
    private lateinit var properties: DockerPdfGeneratorProperties
    private lateinit var meterRegistry: SimpleMeterRegistry
    private lateinit var adapter: DockerPdfGenerator

    private val latexSource = """\documentclass{article}\begin{document}Hello World\end{document}"""

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        dockerClient = mockk(relaxed = true)
        properties = DockerPdfGeneratorProperties(
            image = "ghcr.io/dallay/texlive:2025", // Updated to match the specific version
            maxConcurrentContainers = 10,
            timeoutSeconds = 30,
            memoryLimitMb = 512,
            cpuQuota = 0.5,
            containerUser = "999",
        )
        meterRegistry = SimpleMeterRegistry()
        adapter = DockerPdfGenerator(dockerClient, properties, meterRegistry)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should stream PDF bytes and remove temporary artifacts`() {
        val containerId = "test-container-123"
        val pdfContent = "fake-pdf-content".toByteArray()

        // Track the actual temp directory created by the adapter
        var actualTempDir: Path? = null
        val createContainerCmd = mockk<CreateContainerCmd>(relaxed = true)
        val createContainerResponse = mockk<CreateContainerResponse>()

        every { createContainerResponse.id } returns containerId
        every { dockerClient.createContainerCmd(any()) } returns createContainerCmd
        every { createContainerCmd.withUser(any()) } returns createContainerCmd
        every { createContainerCmd.withWorkingDir(any()) } returns createContainerCmd
        every { createContainerCmd.withCmd(any(), any(), any(), any()) } returns createContainerCmd
        every { createContainerCmd.withHostConfig(any()) } returns createContainerCmd
        every { createContainerCmd.withTty(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStdout(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStderr(any()) } returns createContainerCmd
        every { createContainerCmd.exec() } answers {
            // Capture the temp dir using .use {} to properly close the stream
            actualTempDir = Files.list(Path.of(System.getProperty("java.io.tmpdir"))).use { stream ->
                stream.filter { it.fileName.toString().startsWith("resume-pdf-") }
                    .findFirst()
                    .orElse(null)
            }

            // Create the PDF file in the captured directory
            actualTempDir?.let { dir ->
                Files.write(dir.resolve("resume.pdf"), pdfContent)
            }

            createContainerResponse
        }

        val inspectImageCmd = mockk<InspectImageCmd>(relaxed = true)
        every { dockerClient.inspectImageCmd(any()) } returns inspectImageCmd
        every { inspectImageCmd.exec() } returns mockk()

        val startContainerCmd = mockk<StartContainerCmd>(relaxed = true)
        every { dockerClient.startContainerCmd(containerId) } returns startContainerCmd
        every { startContainerCmd.exec() } returns mockk()

        val inspectContainerCmd = mockk<InspectContainerCmd>(relaxed = true)
        every { dockerClient.inspectContainerCmd(containerId) } returns inspectContainerCmd

        val callCounter = AtomicInteger()
        every { inspectContainerCmd.exec() } answers {
            val response = mockk<InspectContainerResponse>()
            val state = mockk<InspectContainerResponse.ContainerState>()
            if (callCounter.getAndIncrement() == 0) {
                every { state.running } returns true
                every { state.exitCodeLong } returns null
            } else {
                every { state.running } returns false
                every { state.exitCodeLong } returns 0L
            }
            every { response.state } returns state
            response
        }

        val stopContainerCmd = mockk<StopContainerCmd>(relaxed = true)
        every { dockerClient.stopContainerCmd(containerId) } returns stopContainerCmd
        every { stopContainerCmd.withTimeout(any()) } returns stopContainerCmd
        every { stopContainerCmd.exec() } returns mockk()

        val removeContainerCmd = mockk<RemoveContainerCmd>(relaxed = true)
        every { dockerClient.removeContainerCmd(containerId) } returns removeContainerCmd
        every { removeContainerCmd.withForce(any()) } returns removeContainerCmd
        every { removeContainerCmd.exec() } returns mockk()

        val resultStream = adapter.generatePdf(latexSource, "en").block()

        resultStream.shouldBeInstanceOf<ByteArrayInputStream>()
        requireNotNull(resultStream).readAllBytes() shouldBe pdfContent

        // Verify cleanup happened - actualTempDir must exist during test and be cleaned up
        val tempDir = requireNotNull(actualTempDir) { "Temp directory should have been created" }
        Files.exists(tempDir) shouldBe false

        verify(exactly = 1) { dockerClient.removeContainerCmd(containerId) }
    }

    @Test
    fun `should cleanup temporary directory when PDF generation fails`() {
        val containerId = "failure-container"

        var actualTempDir: Path? = null
        val createContainerCmd = mockk<CreateContainerCmd>(relaxed = true)
        val createContainerResponse = mockk<CreateContainerResponse>()

        every { createContainerResponse.id } returns containerId
        every { dockerClient.createContainerCmd(any()) } returns createContainerCmd
        every { createContainerCmd.withUser(any()) } returns createContainerCmd
        every { createContainerCmd.withWorkingDir(any()) } returns createContainerCmd
        every { createContainerCmd.withCmd(any(), any(), any(), any()) } returns createContainerCmd
        every { createContainerCmd.withHostConfig(any()) } returns createContainerCmd
        every { createContainerCmd.withTty(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStdout(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStderr(any()) } returns createContainerCmd
        every { createContainerCmd.exec() } answers {
            // Capture the temp dir using .use {} to properly close the stream
            // Simulate failure: DON'T create the PDF file
            actualTempDir = Files.list(Path.of(System.getProperty("java.io.tmpdir"))).use { stream ->
                stream.filter { it.fileName.toString().startsWith("resume-pdf-") }
                    .findFirst()
                    .orElse(null)
            }

            createContainerResponse
        }

        val inspectImageCmd = mockk<InspectImageCmd>(relaxed = true)
        every { dockerClient.inspectImageCmd(any()) } returns inspectImageCmd
        every { inspectImageCmd.exec() } returns mockk()

        val startContainerCmd = mockk<StartContainerCmd>(relaxed = true)
        every { dockerClient.startContainerCmd(containerId) } returns startContainerCmd
        every { startContainerCmd.exec() } returns mockk()

        val inspectContainerCmd = mockk<InspectContainerCmd>(relaxed = true)
        every { dockerClient.inspectContainerCmd(containerId) } returns inspectContainerCmd

        val callCounter = AtomicInteger()
        every { inspectContainerCmd.exec() } answers {
            val response = mockk<InspectContainerResponse>()
            val state = mockk<InspectContainerResponse.ContainerState>()
            if (callCounter.getAndIncrement() == 0) {
                every { state.running } returns true
                every { state.exitCodeLong } returns null
            } else {
                // Simulate successful exit but missing PDF file
                every { state.running } returns false
                every { state.exitCodeLong } returns 0L
            }
            every { response.state } returns state
            response
        }

        val stopContainerCmd = mockk<StopContainerCmd>(relaxed = true)
        every { dockerClient.stopContainerCmd(containerId) } returns stopContainerCmd
        every { stopContainerCmd.withTimeout(any()) } returns stopContainerCmd
        every { stopContainerCmd.exec() } returns mockk()

        val removeContainerCmd = mockk<RemoveContainerCmd>(relaxed = true)
        every { dockerClient.removeContainerCmd(containerId) } returns removeContainerCmd
        every { removeContainerCmd.withForce(any()) } returns removeContainerCmd
        every { removeContainerCmd.exec() } returns mockk()

        val exception = shouldThrow<PdfGenerationException> {
            adapter.generatePdf(latexSource, "en").block()
        }

        exception.message shouldContain "PDF file was not generated"

        // Verify cleanup happened even on failure - actualTempDir must exist during test and be cleaned up
        val tempDir = requireNotNull(actualTempDir) { "Temp directory should have been created" }
        Files.exists(tempDir) shouldBe false

        verify(exactly = 1) { dockerClient.removeContainerCmd(containerId) }
    }

    @Test
    fun `should throw timeout exception when container execution exceeds limit`() {
        properties = DockerPdfGeneratorProperties(
            image = "ghcr.io/dallay/texlive:2025",
            maxConcurrentContainers = 1,
            timeoutSeconds = 1,
            memoryLimitMb = 512,
            cpuQuota = 0.5,
            containerUser = "999",
        )
        adapter = DockerPdfGenerator(dockerClient, properties, meterRegistry)

        var actualTempDir: Path? = null
        val containerId = "timeout-container"

        val createContainerCmd = mockk<CreateContainerCmd>(relaxed = true)
        val createContainerResponse = mockk<CreateContainerResponse>()

        every { createContainerResponse.id } returns containerId
        every { dockerClient.createContainerCmd(any()) } returns createContainerCmd
        every { createContainerCmd.withUser(any()) } returns createContainerCmd
        every { createContainerCmd.withWorkingDir(any()) } returns createContainerCmd
        every { createContainerCmd.withCmd(any(), any(), any(), any()) } returns createContainerCmd
        every { createContainerCmd.withHostConfig(any()) } returns createContainerCmd
        every { createContainerCmd.withTty(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStdout(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStderr(any()) } returns createContainerCmd
        every { createContainerCmd.exec() } answers {
            // Capture the temp dir using .use {} to properly close the stream
            actualTempDir = Files.list(Path.of(System.getProperty("java.io.tmpdir"))).use { stream ->
                stream.filter { it.fileName.toString().startsWith("resume-pdf-") }
                    .findFirst()
                    .orElse(null)
            }

            createContainerResponse
        }

        val inspectImageCmd = mockk<InspectImageCmd>(relaxed = true)
        every { dockerClient.inspectImageCmd(any()) } returns inspectImageCmd
        every { inspectImageCmd.exec() } returns mockk()

        val startContainerCmd = mockk<StartContainerCmd>(relaxed = true)
        every { dockerClient.startContainerCmd(containerId) } returns startContainerCmd
        every { startContainerCmd.exec() } returns mockk()

        val inspectContainerCmd = mockk<InspectContainerCmd>(relaxed = true)
        every { dockerClient.inspectContainerCmd(containerId) } returns inspectContainerCmd
        every { inspectContainerCmd.exec() } answers {
            val response = mockk<InspectContainerResponse>()
            val state = mockk<InspectContainerResponse.ContainerState>()
            every { state.running } returns true
            every { state.exitCodeLong } returns null
            every { response.state } returns state
            response
        }

        val stopContainerCmd = mockk<StopContainerCmd>(relaxed = true)
        every { dockerClient.stopContainerCmd(containerId) } returns stopContainerCmd
        every { stopContainerCmd.withTimeout(any()) } returns stopContainerCmd
        every { stopContainerCmd.exec() } returns mockk()

        val removeContainerCmd = mockk<RemoveContainerCmd>(relaxed = true)
        every { dockerClient.removeContainerCmd(containerId) } returns removeContainerCmd
        every { removeContainerCmd.withForce(any()) } returns removeContainerCmd
        every { removeContainerCmd.exec() } returns mockk()

        val exception = shouldThrow<PdfGenerationTimeoutException> {
            adapter.generatePdf(latexSource, "en").block()
        }

        exception.message shouldContain "timed out"

        // Verify cleanup happened on timeout - actualTempDir must exist during test and be cleaned up
        val tempDir = requireNotNull(actualTempDir) { "Temp directory should have been created" }
        Files.exists(tempDir) shouldBe false

        verify(exactly = 1) { dockerClient.removeContainerCmd(containerId) }
    }

    @Test
    fun `should retry on transient NoHttpResponseException and succeed`() {
        val containerId = "retry-success-container"
        val pdfContent = "retry-pdf-content".toByteArray()
        val attemptCounter = AtomicInteger(0)

        var actualTempDir: Path? = null
        val createContainerCmd = mockk<CreateContainerCmd>(relaxed = true)
        val createContainerResponse = mockk<CreateContainerResponse>()

        every { createContainerResponse.id } returns containerId
        every { dockerClient.createContainerCmd(any()) } returns createContainerCmd
        every { createContainerCmd.withUser(any()) } returns createContainerCmd
        every { createContainerCmd.withWorkingDir(any()) } returns createContainerCmd
        every { createContainerCmd.withCmd(any(), any(), any(), any()) } returns createContainerCmd
        every { createContainerCmd.withHostConfig(any()) } returns createContainerCmd
        every { createContainerCmd.withTty(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStdout(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStderr(any()) } returns createContainerCmd

        val inspectImageCmd = mockk<InspectImageCmd>(relaxed = true)
        every { dockerClient.inspectImageCmd(any()) } returns inspectImageCmd
        every { inspectImageCmd.exec() } answers {
            val attempt = attemptCounter.incrementAndGet()
            if (attempt <= 2) {
                // First two attempts fail with NoHttpResponseException (transient error)
                // Wrapped in RuntimeException as docker-java client does
                @Suppress("TooGenericExceptionThrown")
                throw RuntimeException(NoHttpResponseException("docker-socket-proxy:2375 failed to respond"))
            }
            // Third attempt succeeds
            mockk()
        }

        every { createContainerCmd.exec() } answers {
            actualTempDir = Files.list(Path.of(System.getProperty("java.io.tmpdir"))).use { stream ->
                stream.filter { it.fileName.toString().startsWith("resume-pdf-") }
                    .findFirst()
                    .orElse(null)
            }

            actualTempDir?.let { dir ->
                Files.write(dir.resolve("resume.pdf"), pdfContent)
            }

            createContainerResponse
        }

        val startContainerCmd = mockk<StartContainerCmd>(relaxed = true)
        every { dockerClient.startContainerCmd(containerId) } returns startContainerCmd
        every { startContainerCmd.exec() } returns mockk()

        val inspectContainerCmd = mockk<InspectContainerCmd>(relaxed = true)
        every { dockerClient.inspectContainerCmd(containerId) } returns inspectContainerCmd

        val inspectCallCounter = AtomicInteger()
        every { inspectContainerCmd.exec() } answers {
            val response = mockk<InspectContainerResponse>()
            val state = mockk<InspectContainerResponse.ContainerState>()
            if (inspectCallCounter.getAndIncrement() == 0) {
                every { state.running } returns true
                every { state.exitCodeLong } returns null
            } else {
                every { state.running } returns false
                every { state.exitCodeLong } returns 0L
            }
            every { response.state } returns state
            response
        }

        val stopContainerCmd = mockk<StopContainerCmd>(relaxed = true)
        every { dockerClient.stopContainerCmd(containerId) } returns stopContainerCmd
        every { stopContainerCmd.withTimeout(any()) } returns stopContainerCmd
        every { stopContainerCmd.exec() } returns mockk()

        val removeContainerCmd = mockk<RemoveContainerCmd>(relaxed = true)
        every { dockerClient.removeContainerCmd(containerId) } returns removeContainerCmd
        every { removeContainerCmd.withForce(any()) } returns removeContainerCmd
        every { removeContainerCmd.exec() } returns mockk()

        val resultStream = adapter.generatePdf(latexSource, "en").block()

        resultStream.shouldBeInstanceOf<ByteArrayInputStream>()
        requireNotNull(resultStream).readAllBytes() shouldBe pdfContent

        // Verify cleanup happened
        val tempDir = requireNotNull(actualTempDir) { "Temp directory should have been created" }
        Files.exists(tempDir) shouldBe false

        verify(exactly = 1) { dockerClient.removeContainerCmd(containerId) }

        // Verify retry metric was incremented (2 retries before success)
        val retryCounter = meterRegistry.counter("docker.container.retry", "component", "pdf-generator")
        retryCounter.count() shouldBe 2.0
    }

    @Test
    fun `should not retry on non-transient errors`() {
        val inspectImageCmd = mockk<InspectImageCmd>(relaxed = true)
        every { dockerClient.inspectImageCmd(any()) } returns inspectImageCmd
        // Non-transient error - should NOT retry
        every { inspectImageCmd.exec() } throws PdfGenerationException("LaTeX compilation error - invalid syntax")

        val exception = shouldThrow<PdfGenerationException> {
            adapter.generatePdf(latexSource, "en").block()
        }

        exception.message shouldContain "LaTeX compilation error"

        // Verify NO retries happened for non-transient errors
        val retryCounter = meterRegistry.counter("docker.container.retry", "component", "pdf-generator")
        retryCounter.count() shouldBe 0.0
    }
}
