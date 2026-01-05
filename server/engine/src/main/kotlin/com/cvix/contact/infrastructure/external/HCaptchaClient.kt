package com.cvix.contact.infrastructure.external

import com.cvix.contact.domain.CaptchaValidator
import com.cvix.contact.infrastructure.config.ContactProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
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
 * @property webClient WebClient for making HTTP requests.
 * @property properties Configuration properties containing hCaptcha secret key.
 */
@Component
class HCaptchaClient(
    private val webClient: WebClient,
    private val properties: ContactProperties
) : CaptchaValidator {

    /**
     * Verifies an hCaptcha token with the hCaptcha API.
     *
     * @param token The hCaptcha response token from the client.
     * @param ipAddress The user's IP address.
     * @return true if the token is valid, false otherwise.
     */
    override suspend fun verify(token: String, ipAddress: String): Boolean {
        logger.info("Verifying hCaptcha token from IP: {}", ipAddress)

        return try {
            val response = webClient.post()
                .uri("https://hcaptcha.com/siteverify")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(buildVerificationBody(token, ipAddress))
                .retrieve()
                .bodyToMono<HCaptchaVerificationResponse>()
                .awaitSingle()

            if (response.success) {
                logger.info("hCaptcha verification successful")
            } else {
                logger.warn("hCaptcha verification failed: {}", response.errorCodes)
            }

            response.success
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
     */
    private fun buildVerificationBody(token: String, ipAddress: String): String {
        return listOf(
            "secret=${properties.hcaptchaSecretKey}",
            "response=$token",
            "remoteip=$ipAddress",
        ).joinToString("&")
    }
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
