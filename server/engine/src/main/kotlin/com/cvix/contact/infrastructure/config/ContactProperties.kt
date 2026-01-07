package com.cvix.contact.infrastructure.config

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Configuration properties for the contact form feature.
 *
 * These properties are loaded from application.yml and environment variables.
 * All sensitive values (API keys, tokens) should be provided via environment variables.
 *
 * Example configuration in application.yml:
 * ```yaml
 * application:
 *   contact:
 *     webhook-url: ${CONTACT_WEBHOOK_URL}
 *     header-api-key: ${CONTACT_HEADER_API_KEY}
 *     api-key: ${CONTACT_API_KEY}
 *     form-token-id: ${CONTACT_FORM_TOKEN_ID}
 *     hcaptcha-secret-key: ${HCAPTCHA_SECRET_KEY}
 *     webhook-timeout-seconds: 10
 * ```
 *
 * @property webhookUrl The full URL to the n8n webhook endpoint.
 * @property headerApiKey The API key sent via x-api-key header for n8n authentication.
 * @property apiKey The API key sent via Authorization Bearer token (if required by n8n workflow).
 * @property formTokenId The form-specific token for domain validation (form-token-id header).
 * @property hcaptchaSecretKey The secret key for server-side hCaptcha validation.
 * @property webhookTimeoutSeconds Timeout in seconds for webhook HTTP requests (default: 10).
 */
@ConfigurationProperties(prefix = "application.contact")
@Validated
data class ContactProperties(
    @field:NotBlank(message = "Webhook URL is required")
    @field:URL(message = "Webhook URL must be a valid URL")
    val webhookUrl: String,

    @field:NotBlank(message = "Header API key name is required")
    val headerApiKey: String,

    @field:NotBlank(message = "API key is required")
    val apiKey: String,

    @field:NotBlank(message = "Form token ID is required")
    val formTokenId: String,

    @field:NotBlank(message = "hCaptcha secret key is required")
    val hcaptchaSecretKey: String,

    val webhookTimeoutSeconds: Long = 10
)
