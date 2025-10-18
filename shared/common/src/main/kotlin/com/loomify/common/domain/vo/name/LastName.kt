package com.loomify.common.domain.vo.name

import com.loomify.common.domain.BaseValidateValueObject

/**
 * LastName value object
 * @param lastname last name value
 * @throws LastNameNotValidException if last name is not valid
 * @see BaseValidateValueObject
 * @see com.loomify.common.domain.BaseValidateValueObject
 * @see LastNameNotValidException
 */
data class LastName(val lastname: String) : BaseValidateValueObject<String>(lastname) {
    /**
     * Validate last name value object
     * @param value last name value
     * @throws LastNameNotValidException if last name is not valid
     */
    override fun validate(value: String) {
        val lastname = value.trim()
        if (lastname.isEmpty() || lastname.length > NAME_LEN) {
            throw LastNameNotValidException(value)
        }
    }

    override fun toString(): String = lastname
}
