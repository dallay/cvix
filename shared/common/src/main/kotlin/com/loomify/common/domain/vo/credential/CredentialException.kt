package com.loomify.common.domain.vo.credential

import com.loomify.common.domain.error.BusinessRuleValidationException

class CredentialException(
    override val message: String,
    override val cause: Throwable? = null
) : BusinessRuleValidationException(message, cause)
