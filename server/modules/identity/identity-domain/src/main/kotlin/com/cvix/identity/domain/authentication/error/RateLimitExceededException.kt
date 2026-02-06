package com.cvix.identity.domain.authentication.error

import java.time.Duration

class RateLimitExceededException(val retryAfter: Duration) : RuntimeException("Rate limit exceeded")
