package com.cvix.subscriber.domain

/**
 * Enum representing the possible statuses of a subscriber.
 *
 * - ENABLED: The subscriber is active and enabled.
 * - DISABLED: The subscriber is inactive or disabled.
 * - BLOCKLISTED: The subscriber is blocklisted and cannot participate.
 * - PENDING: The subscriber's status is pending confirmation or activation.
 * - CONFIRMED: The subscriber has been confirmed.
 * - EXPIRED: The subscriber's status has expired.
 */
enum class SubscriberStatus {
    ENABLED, // Subscriber is active and enabled
    DISABLED, // Subscriber is inactive or disabled
    BLOCKLISTED, // Subscriber is blocklisted
    PENDING, // Subscriber is pending confirmation or activation
    CONFIRMED, // Subscriber has been confirmed
    EXPIRED // Subscriber's status has expired
}
