package com.loomify.common.domain.vo.name

import com.loomify.common.domain.BaseValidateValueObject

/**
 * FirstName value object
 * @param firstname first name value
 * @throws FirstNameNotValidException if first name is not valid
 * @see BaseValidateValueObject
 * @see com.loomify.common.domain.BaseValidateValueObject
 * @see FirstNameNotValidException
 */
data class FirstName(val firstname: String) : BaseValidateValueObject<String>(firstname) {
    /**
     * Validate first name value object with regex
     * @param value first name value
     * @throws FirstNameNotValidException if first name is not valid
     */
    override fun validate(value: String) {
        val firstname = value.trim()
        if (firstname.isEmpty() || firstname.length > NAME_LEN) {
            throw FirstNameNotValidException(value)
        }
    }

    override fun toString(): String = firstname
}
