package com.cvix.common.domain.vo.credential

import com.cvix.common.domain.error.BusinessRuleValidationException

class CredentialException(
    override val message: String,
    override val cause: Throwable? = null
) : BusinessRuleValidationException(message, cause)
