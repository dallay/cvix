package com.loomify.common.domain.error

import com.loomify.common.domain.presentation.pagination.LogicalOperator

/**
 * Invalid filter operator exception is thrown when a filter operator is not valid.
 * The valid filter operators are: [LogicalOperator.AND], [LogicalOperator.OR].
 * @param message The exception message.
 */
data class InvalidFilterOperator(override val message: String) :
    BusinessRuleValidationException(message)
