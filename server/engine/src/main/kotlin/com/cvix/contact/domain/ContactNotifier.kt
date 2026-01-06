package com.cvix.contact.domain

/**
 * Port interface for sending contact notifications to external systems.
 *
 * This is a domain-level abstraction that defines the contract for notifying
 * about new contact form submissions without coupling to any specific
 * implementation (N8N, email service, Slack, etc.).
 *
 * The infrastructure layer will provide the concrete implementation.
 */
fun interface ContactNotifier {
    /**
     * Sends a contact notification to the configured external system.
     *
     * @param contactData The contact information to send
     * @throws ContactNotificationException if the notification fails
     */
    suspend fun notify(contactData: ContactData)
}
