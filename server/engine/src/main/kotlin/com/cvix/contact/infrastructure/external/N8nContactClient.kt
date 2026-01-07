package com.cvix.contact.infrastructure.external

import com.cvix.contact.domain.ContactData
import com.cvix.contact.domain.ContactNotificationException
import com.cvix.contact.domain.ContactNotifier
import com.cvix.contact.infrastructure.config.ContactProperties
import java.time.Duration
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

private val logger = LoggerFactory.getLogger(N8nContactClient::class.java)

private const val TIMEOUT = 10L

/**
 * Infrastructure adapter implementing contact notification via N8N webhook.
 *
 * This adapter implements the ContactNotifier port by sending contact data
 * to an N8N workflow via authenticated webhook.
 *
 * Authentication uses two headers:
 * - x-api-key: Header Auth credential (validates webhook access)
 * - form-token-id: Form-specific token (validates domain/origin)
 *
 * @property webClient WebClient for making HTTP requests.
 * @property properties Configuration properties containing n8n URL and credentials.
 */
@Component
class N8nContactClient(
    private val webClient: WebClient,
    private val properties: ContactProperties
) : ContactNotifier {

    /**
     * Sends contact notification to n8n webhook.
     *
     * @param contactData The contact information from the domain.
     * @throws ContactNotificationException if the notification fails.
     */
    override suspend fun notify(contactData: ContactData) {
        logger.info("Sending contact notification to n8n")

        try {
            // Map domain ContactData to N8N-specific payload
            val n8nPayload = N8nContactPayload(
                name = contactData.name,
                email = contactData.email,
                subject = contactData.subject,
                message = contactData.message,
            )

            val response = webClient.post()
                .uri(properties.webhookUrl)
                .header(properties.headerApiKey, properties.apiKey)
                .header("form-token-id", properties.formTokenId)
                .bodyValue(n8nPayload)
                .retrieve()
                .bodyToMono<N8nWebhookResponse>()
                .timeout(Duration.ofSeconds(TIMEOUT))
                .awaitSingle()

            when {
                response.status == "success" ->
                    logger.info("Contact notification sent successfully to n8n: email={}", contactData.email)

                response.error != null -> {
                    logger.error("n8n webhook returned error: {}", response.error)
                    throw ContactNotificationException("n8n webhook error: ${response.error}")
                }
                else -> {
                    logger.error("n8n webhook returned unexpected response: {}", response)
                    throw ContactNotificationException("Unexpected n8n webhook response")
                }
            }
        } catch (e: ContactNotificationException) {
            throw e
        } catch (e: WebClientResponseException) {
            logger.error("n8n webhook returned error response: {} {}", e.statusCode, e.statusText, e)
            throw ContactNotificationException("n8n webhook returned error: ${e.statusCode}", e)
        } catch (e: WebClientRequestException) {
            logger.error("Failed to connect to n8n webhook", e)
            throw ContactNotificationException("Failed to connect to n8n webhook", e)
        }
    }
}

/**
 * Internal payload structure for N8N webhook.
 * This is infrastructure-specific and not exposed to the application layer.
 * @property name Sender's full name.
 * @property email Sender's email address.
 * @property subject Message subject.
 * @property message Message content.
 */
private data class N8nContactPayload(
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
)

/**
 * Response structure from N8N webhook.
 * Success response contains "status": "success"
 * Error response contains "error": "error message"
 * @property status Status of the webhook execution (present on success).
 * @property error Error message (present on failure).
 */
private data class N8nWebhookResponse(
    val status: String? = null,
    val error: String? = null,
)
