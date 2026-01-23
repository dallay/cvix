package com.cvix.subscriber.domain.exceptions

import com.cvix.common.domain.error.BusinessRuleValidationException

sealed class SubscriberException(message: String, cause: Throwable? = null) :
    BusinessRuleValidationException(message, cause)

class SubscriberNotFoundException(subscriberId: String) :
    SubscriberException("Subscriber with ID $subscriberId not found.")

class InvalidSubscriberDataException(reason: String) :
    SubscriberException("Invalid subscriber data: $reason")
