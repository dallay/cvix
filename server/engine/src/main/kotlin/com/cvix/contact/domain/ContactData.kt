package com.cvix.contact.domain

/**
 * Domain model representing contact form data.
 *
 * This is a pure domain object with no framework dependencies.
 * It represents the essential information from a contact form submission.
 * @property name The full name of the sender.
 * @property email The email address of the sender.
 * @property subject The subject of the message.
 * @property message The content of the message.
 */
data class ContactData(
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
)
