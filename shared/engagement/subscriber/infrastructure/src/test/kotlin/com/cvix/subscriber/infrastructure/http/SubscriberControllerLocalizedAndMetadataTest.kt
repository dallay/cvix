package com.cvix.subscriber.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.UnitTest
import com.cvix.subscriber.application.create.CreateSubscriberCommand
import com.cvix.subscriber.infrastructure.http.request.SubscriberRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@UnitTest
internal class SubscriberControllerLocalizedAndMetadataTest : ControllerTest() {
    private val controller = SubscriberController(mediator, messageSource)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)

    @Test
    fun `should use Accept-Language header when present`() {
        val request = SubscriberRequest(email = "a@b.com", source = "src", language = "en")

        every { messageSource.getMessage(any(), any(), any<java.util.Locale>()) } returns "Succès"
        coEvery { mediator.send(any<CreateSubscriberCommand>()) } returns Unit

        webTestClient.post().uri("/api/subscribers")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "fr")
            .header("X-Workspace-Id", UUID.randomUUID().toString())
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().valueMatches("Location", ".*/api/subscribers/.*")
            .expectBody()

        coVerify(exactly = 1) { mediator.send(any<CreateSubscriberCommand>()) }
    }

    @Test
    fun `should default to English when Accept-Language missing`() {
        val request = SubscriberRequest(email = "a@b.com", source = "src", language = "en")

        coEvery { mediator.send(any<CreateSubscriberCommand>()) } returns Unit

        webTestClient.post().uri("/api/subscribers")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Workspace-Id", UUID.randomUUID().toString())
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated

        coVerify(exactly = 1) { mediator.send(any<CreateSubscriberCommand>()) }
    }

    @Test
    fun `should include metadata userAgent and referer based on headers`() {
        val request = SubscriberRequest(email = "a@b.com", source = "src", language = "en")

        val captured = mutableListOf<CreateSubscriberCommand>()
        coEvery { mediator.send(capture(captured)) } answers {
            captured.add(firstArg())
            Unit
        }

        webTestClient.post().uri("/api/subscribers")
            .contentType(MediaType.APPLICATION_JSON)
            .header("User-Agent", "kt-agent")
            .header("Referer", "https://example.com")
            .header("X-Workspace-Id", UUID.randomUUID().toString())
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated

        // Assert metadata is present in the captured command
        val cmd = captured.first()
        assert(cmd.attributes?.metadata?.get("userAgent") == "kt-agent")
        assert(cmd.attributes?.metadata?.get("referer") == "https://example.com")
    }
}
