package com.cvix.common.domain.criteria

/**
 *
 * @created 10/1/24
 */
fun interface CriteriaParser<Out : Any?> {
    fun parse(criteria: Criteria): Out
}
