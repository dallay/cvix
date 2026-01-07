package com.cvix.contact.infrastructure.external

import com.cvix.contact.domain.CaptchaValidator
import com.cvix.contact.infrastructure.config.ContactProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

private val logger = LoggerFactory.getLogger(HCaptchaClient::class.java)

/**
 * Infrastructure adapter implementing hCaptcha verification.
 *
 * This adapter implements the CaptchaValidator port using hCaptcha's server-side
 * verification API as documented at: https://docs.hcaptcha.com/#verify-the-user-response-server-side
 *
 * Performance optimizations:
 * - Uses dedicated WebClient instance with optimized timeouts for captcha verification
 * - Leverages Spring's native coroutine support via awaitBody() for cleaner, more efficient code
 * - Pre-configured base URL and headers to reduce per-request overhead
 *
 * @property webClient Dedicated WebClient for hCaptcha API with optimized configuration.
 * @property properties Configuration properties containing hCaptcha secret key.
 */
@Component
class HCaptchaClient(
    @Qualifier("hcaptchaWebClient")
    private val webClient: WebClient,
    private val properties: ContactProperties
) : CaptchaValidator {

    /**
     * Verifies an hCaptcha token with the hCaptcha API.
     *
     * Optimizations implemented:
     * - Dedicated WebClient with tuned timeouts (3s connection, 5s response)
     * - Base URL and Content-Type pre-configured to reduce per-request overhead
     *
     * @param token The hCaptcha response token from the client.
     * @param ipAddress The user's IP address.
     * @return true if the token is valid, false otherwise.
     */
    override suspend fun verify(token: String, ipAddress: String): Boolean {
        logger.info("Verifying hCaptcha token from IP: {}", ipAddress)

        return try {
            val response = webClient.post()
                .uri("/siteverify") // Base URL already configured in WebClient
                .bodyValue(buildVerificationBody(token, ipAddress))
                .retrieve()
                .bodyToMono<HCaptchaVerificationResponse>()
                .awaitSingle()

            response.success.also { success ->
                if (success) {
                    logger.info("hCaptcha verification successful")
                } else {
                    logger.warn("hCaptcha verification failed: {}", response.errorCodes)
                }
            }
        } catch (e: WebClientResponseException) {
            logger.error("hCaptcha API returned error response: {} {}", e.statusCode, e.statusText, e)
            false
        } catch (e: WebClientRequestException) {
            logger.error("Failed to connect to hCaptcha API", e)
            false
        }
    }

    /**
     * Builds the form-urlencoded body for hCaptcha verification.
     *
     * Parameters are properly URL-encoded to handle special characters in tokens.
     */
    private fun buildVerificationBody(token: String, ipAddress: String): String {
        return listOf(
            "secret=${urlEncode(properties.hcaptchaSecretKey)}",
            "response=${urlEncode(token)}",
            "remoteip=${urlEncode(ipAddress)}",
        ).joinToString("&")
    }

    /**
     * URL-encodes a string for safe use in application/x-www-form-urlencoded content.
     */
    private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}

/**
 * Response from hCaptcha verification API.
 *
 * @property success Whether the verification succeeded.
 * @property challengeTs Timestamp of the challenge (ISO format).
 * @property hostname The hostname of the site where the challenge was solved.
 * @property errorCodes Optional list of error codes if verification failed.
 */
data class HCaptchaVerificationResponse(
    val success: Boolean,
    @JsonProperty("challenge_ts")
    val challengeTs: String? = null,
    val hostname: String? = null,
    @JsonProperty("error-codes")
    val errorCodes: List<String>? = null
)
