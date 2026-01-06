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
    /**
     * Sends contact submission data to an external notification system.
     *
     * @param contactData The contact information to deliver (e.g., name, email, message).
     * @throws ContactNotificationException If the notification could not be delivered.
     */
    suspend fun notify(contactData: ContactData)
}
