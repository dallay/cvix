package com.cvix.contact.domain

/**
 * Port interface for validating CAPTCHA tokens.
 *
 * This is a domain-level abstraction that defines the contract for CAPTCHA validation
 * without coupling to any specific implementation (hCaptcha, reCAPTCHA, etc.).
 *
 * The infrastructure layer will provide the concrete implementation.
 */
fun interface CaptchaValidator {
    /**
     * Verifies a CAPTCHA token.
     *
     * @param token The CAPTCHA token to validate
     * @param ipAddress The IP address of the client submitting the token
     * @return true if the token is valid and verified by the CAPTCHA service,
     *         false if the token is invalid, expired, or rejected by the CAPTCHA service
     * @throws CaptchaValidationException if validation fails due to infrastructure issues
     *         (network errors, service unavailability, configuration problems, etc.)
     */
    suspend fun verify(token: String, ipAddress: String): Boolean
}
