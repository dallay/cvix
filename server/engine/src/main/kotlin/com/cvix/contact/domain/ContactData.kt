package com.cvix.contact.domain

/**
 * Domain model representing contact form data.
 *
 * This is a pure domain object with no framework dependencies.
 * It represents the essential information from a contact form submission.
 */
data class ContactData(
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
    val hcaptchaToken: String
)
