package com.cvix.authentication.domain.error

import java.time.Duration

class RateLimitExceededException(val retryAfter: Duration) : RuntimeException("Rate limit exceeded")
