package com.cvix.contact.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

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
 * ```
 *
 * @property webhookUrl The full URL to the n8n webhook endpoint.
 * @property headerApiKey The API key sent via x-api-key header for n8n authentication.
 * @property apiKey The API key sent via Authorization Bearer token (if required by n8n workflow).
 * @property formTokenId The form-specific token for domain validation (form-token-id header).
 * @property hcaptchaSecretKey The secret key for server-side hCaptcha validation.
 */
@ConfigurationProperties(prefix = "application.contact")
data class ContactProperties(
    val webhookUrl: String,
    val headerApiKey: String,
    val apiKey: String,
    val formTokenId: String,
    val hcaptchaSecretKey: String
)
