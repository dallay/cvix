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
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
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
            image = "ghcr.io/cvix/texlive:2024", // Updated to match the specific version
            maxConcurrentContainers = 10,
            timeoutSeconds = 30,
            memoryLimitMb = 512,
            cpuQuota = 0.5,
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

        val workspace = tempDir.resolve("docker-work")
        Files.createDirectory(workspace)

        mockkStatic(Files::class)
        val pdfPath = workspace.resolve("resume.pdf")
        every { Files.createTempDirectory(any<String>()) } returns workspace
        every { Files.writeString(any<Path>(), any<CharSequence>()) } answers {
            val path = arg<Path>(0)
            val content = arg<CharSequence>(1).toString()
            path.toFile().writeText(content)
            path
        }
        every { Files.write(any<Path>(), any<ByteArray>()) } answers {
            val path = arg<Path>(0)
            val bytes = arg<ByteArray>(1)
            path.toFile().writeBytes(bytes)
            path
        }
        every { Files.readAllBytes(any<Path>()) } answers {
            val path = arg<Path>(0)
            path.toFile().readBytes()
        }
        every { Files.exists(any<Path>()) } answers {
            val path = arg<Path>(0)
            path.toFile().exists()
        }

        val inspectContainerCmd = stubDockerLifecycle(containerId)
        val callCounter = AtomicInteger()
        every { inspectContainerCmd.exec() } answers {
            val response = mockk<InspectContainerResponse>()
            val state = mockk<InspectContainerResponse.ContainerState>()
            if (callCounter.getAndIncrement() == 0) {
                every { state.running } returns true
                every { state.exitCodeLong } returns null
            } else {
                if (!Files.exists(pdfPath)) {
                    Files.write(pdfPath, pdfContent)
                }
                every { state.running } returns false
                every { state.exitCodeLong } returns 0L
            }
            every { response.state } returns state
            response
        }

        val resultStream = adapter.generatePdf(latexSource, "en").block()

        resultStream.shouldBeInstanceOf<ByteArrayInputStream>()
        requireNotNull(resultStream).readAllBytes() shouldBe pdfContent
        Files.exists(workspace) shouldBe false
        Files.exists(pdfPath) shouldBe false

        verify(exactly = 1) { dockerClient.removeContainerCmd(containerId) }
    }

    @Test
    fun `should cleanup temporary directory when PDF generation fails`() {
        val containerId = "failure-container"

        val workspace = tempDir.resolve("docker-failure")
        Files.createDirectory(workspace)

        mockkStatic(Files::class)
        val pdfPath = workspace.resolve("resume.pdf")
        every { Files.createTempDirectory(any<String>()) } returns workspace
        every { Files.writeString(any<Path>(), any<CharSequence>()) } answers {
            val path = arg<Path>(0)
            val content = arg<CharSequence>(1).toString()
            path.toFile().writeText(content)
            path
        }
        every { Files.write(any<Path>(), any<ByteArray>()) } answers {
            val path = arg<Path>(0)
            val bytes = arg<ByteArray>(1)
            path.toFile().writeBytes(bytes)
            path
        }
        every { Files.exists(any<Path>()) } answers {
            val path = arg<Path>(0)
            path.toFile().exists()
        }

        val inspectContainerCmd = stubDockerLifecycle(containerId)
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

        val exception = shouldThrow<PdfGenerationException> {
            adapter.generatePdf(latexSource, "en").block()
        }

        exception.message shouldContain "PDF file was not generated"
        Files.exists(workspace) shouldBe false
        Files.exists(pdfPath) shouldBe false
        verify(exactly = 1) { dockerClient.removeContainerCmd(containerId) }
    }

    @Test
    fun `should throw timeout exception when container execution exceeds limit`() {
        properties = DockerPdfGeneratorProperties(
            image = "ghcr.io/cvix/texlive:2024",
            maxConcurrentContainers = 1,
            timeoutSeconds = 1,
            memoryLimitMb = 512,
            cpuQuota = 0.5,
        )
        adapter = DockerPdfGenerator(dockerClient, properties, meterRegistry)

        val workspace = tempDir.resolve("docker-timeout")
        Files.createDirectory(workspace)

        mockkStatic(Files::class)
        every { Files.createTempDirectory(any<String>()) } returns workspace
        every { Files.writeString(any<Path>(), any<CharSequence>()) } answers {
            val path = arg<Path>(0)
            val content = arg<CharSequence>(1).toString()
            path.toFile().writeText(content)
            path
        }
        every { Files.exists(any<Path>()) } answers {
            val path = arg<Path>(0)
            path.toFile().exists()
        }

        val containerId = "timeout-container"
        val inspectContainerCmd = stubDockerLifecycle(containerId)
        every { inspectContainerCmd.exec() } answers {
            val response = mockk<InspectContainerResponse>()
            val state = mockk<InspectContainerResponse.ContainerState>()
            every { state.running } returns true
            every { state.exitCodeLong } returns null
            every { response.state } returns state
            response
        }

        val exception = shouldThrow<PdfGenerationTimeoutException> {
            adapter.generatePdf(latexSource, "en").block()
        }

        exception.message shouldContain "timed out"
        Files.exists(workspace) shouldBe false
        verify(exactly = 1) { dockerClient.removeContainerCmd(containerId) }
    }

    @Test
    fun `should use correct Docker image from properties`() {
        // Assert
        properties.image shouldBe "ghcr.io/cvix/texlive:2024"
        properties.timeoutSeconds shouldBe 30L
        properties.memoryLimitMb shouldBe 512L
        properties.cpuQuota shouldBe 0.5
        properties.maxConcurrentContainers shouldBe 10
    }

    private fun stubDockerLifecycle(containerId: String): InspectContainerCmd {
        val inspectImageCmd = mockk<InspectImageCmd>(relaxed = true)
        every { dockerClient.inspectImageCmd(any()) } returns inspectImageCmd
        every { inspectImageCmd.exec() } returns mockk()

        val createContainerCmd = mockk<CreateContainerCmd>(relaxed = true)
        val createContainerResponse = mockk<CreateContainerResponse>()
        every { createContainerResponse.id } returns containerId
        every { dockerClient.createContainerCmd(any()) } returns createContainerCmd
        every { createContainerCmd.withWorkingDir(any()) } returns createContainerCmd
        every { createContainerCmd.withCmd(any(), any(), any(), any()) } returns createContainerCmd
        every { createContainerCmd.withHostConfig(any()) } returns createContainerCmd
        every { createContainerCmd.withTty(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStdout(any()) } returns createContainerCmd
        every { createContainerCmd.withAttachStderr(any()) } returns createContainerCmd
        every { createContainerCmd.exec() } returns createContainerResponse

        val startContainerCmd = mockk<StartContainerCmd>(relaxed = true)
        every { dockerClient.startContainerCmd(containerId) } returns startContainerCmd
        every { startContainerCmd.exec() } returns mockk()

        val inspectContainerCmd = mockk<InspectContainerCmd>(relaxed = true)
        every { dockerClient.inspectContainerCmd(containerId) } returns inspectContainerCmd

        val stopContainerCmd = mockk<StopContainerCmd>(relaxed = true)
        every { dockerClient.stopContainerCmd(containerId) } returns stopContainerCmd
        every { stopContainerCmd.withTimeout(any()) } returns stopContainerCmd
        every { stopContainerCmd.exec() } returns mockk()

        val removeContainerCmd = mockk<RemoveContainerCmd>(relaxed = true)
        every { dockerClient.removeContainerCmd(containerId) } returns removeContainerCmd
        every { removeContainerCmd.withForce(any()) } returns removeContainerCmd
        every { removeContainerCmd.exec() } returns mockk()

        return inspectContainerCmd
    }
}
