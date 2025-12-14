package com.cvix.common.domain.vo.credential

import com.cvix.common.domain.BaseId
import com.cvix.common.domain.vo.credential.Credential.Companion.MIN_LENGTH
import com.cvix.common.domain.vo.credential.Credential.Companion.charLowercase
import com.cvix.common.domain.vo.credential.Credential.Companion.charNumbers
import com.cvix.common.domain.vo.credential.Credential.Companion.charSpecial
import com.cvix.common.domain.vo.credential.Credential.Companion.charUppercase
import com.cvix.common.domain.vo.credential.Credential.Companion.charset
import java.util.UUID

/**
 * Credential representation in the domain layer of the application that is used to authenticate a user
 * @created 2/7/23
 */
data class Credential(
    val id: CredentialId,
    val credentialValue: CredentialValue,
) {
    companion object {
        private const val REQUIRED_TYPES = 4
        const val MIN_LENGTH = 8
        // expose the character sets so other value objects (in this file) can validate passwords
        val charNumbers: CharRange = '0'..'9'
        val charUppercase: CharRange = 'A'..'Z'
        val charLowercase: CharRange = 'a'..'z'
        val charSpecial: List<Char> = "!@#$%^&*()_+{}|:<>?".toList()

        // Updated charset to include special characters for better entropy
        val charset: List<Char> = listOf(charLowercase, charUppercase, charNumbers, charSpecial).flatten()

        /**
         * Generates a random password with the following rules:
         * - Must have at least one number, one uppercase, one lowercase and one special character
         * - Must have at least 8 characters
         * @return the generated password
         * @see MIN_LENGTH the minimum length of the password
         * @see charNumbers the list of numbers
         * @see charUppercase the list of uppercase characters
         * @see charLowercase the list of lowercase characters
         * @see charSpecial the list of special characters
         * @see charset the list of all characters
         * @see kotlin.random.Random the random generator
         * @see kotlin.random.Random.nextInt to generate a random number
         */
        fun generateRandomCredentialPassword(): String {
            val passwordChars = mutableListOf<Char>().apply {
                add(charNumbers.random())
                add(charUppercase.random())
                add(charLowercase.random())
                add(charSpecial.random())
            }

            val minLen = maxOf(MIN_LENGTH, REQUIRED_TYPES)
            val targetLength = minLen + kotlin.random.Random.nextInt(minLen)
            repeat(targetLength - passwordChars.size) {
                passwordChars.add(charset.random())
            }

            return passwordChars.shuffled().joinToString("")
        }

        /**
         * Creates a new credential with the given value
         * @param credentialValue the value of the credential
         * @return the created credential with the given value and a random id generated with [UUID.randomUUID]
         * @see UUID.randomUUID for the id generation
         * @see CredentialId for the id type
         * @see Credential for the credential type
         */
        fun create(
            credentialValue: String
        ): Credential =
            // use the secondary constructor which accepts a raw String
            fromRaw(CredentialId(UUID.randomUUID()), credentialValue)

        /**
         * Creates a Credential from a raw String
         * @param id the id of the credential
         * @param raw the raw String value of the credential
         * @return the created Credential
         */
        fun fromRaw(id: CredentialId, raw: String): Credential = Credential(id, CredentialValue(raw))
    }
}

/**
 * Credential id representation in the domain layer of the application that is used to identify a credential
 * @see BaseId for the base id class
 * @see UUID for the id type
 * @see Credential for the credential type
 * @see CredentialId for the id type
 */
data class CredentialId(override val id: UUID) : BaseId<UUID>(id)

/**
 * Password credential value representation with validation rules for secure passwords.
 *
 * **Note:** This value class is specifically designed for password credentials and enforces:
 * - Minimum length of 8 characters
 * - At least one digit, uppercase letter, lowercase letter, and special character
 *
 * Non-password credential types (API keys, tokens, etc.) should use a different value class.
 */
@JvmInline
value class CredentialValue(val value: String) {
    init {
        if (value.isBlank()) throw CredentialException("Credential value cannot be blank")
        if (value.length < MIN_LENGTH) {
            throw CredentialException(
                "Credential value must be at least $MIN_LENGTH characters",
            )
        }
        if (value.length > MAX_CREDENTIAL_LENGTH) {
            throw CredentialException(
                "Credential value cannot exceed $MAX_CREDENTIAL_LENGTH characters",
            )
        }
        if (!value.any { it in charNumbers }) {
            throw CredentialException(
                "The password must have at least one number",
            )
        }
        if (!value.any { it in charUppercase }) {
            throw CredentialException(
                "The password must have at least one uppercase character",
            )
        }
        if (!value.any { it in charLowercase }) {
            throw CredentialException(
                "The password must have at least one lowercase character",
            )
        }
        if (!value.any { it in charSpecial }) {
            throw CredentialException("The password must have at least one special character")
        }
    }

    override fun toString(): String = "****"

    companion object {
        const val MAX_CREDENTIAL_LENGTH = 128
    }
}
