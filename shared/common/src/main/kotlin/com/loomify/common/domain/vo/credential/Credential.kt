package com.loomify.common.domain.vo.credential

import com.loomify.common.domain.BaseId
import com.loomify.common.domain.vo.credential.Credential.Companion.MIN_LENGTH
import com.loomify.common.domain.vo.credential.Credential.Companion.charLowercase
import com.loomify.common.domain.vo.credential.Credential.Companion.charNumbers
import com.loomify.common.domain.vo.credential.Credential.Companion.charSpecial
import com.loomify.common.domain.vo.credential.Credential.Companion.charUppercase
import com.loomify.common.domain.vo.credential.Credential.Companion.charset
import java.util.*

/**
 * Credential representation in the domain layer of the application that is used to authenticate a user
 * @created 2/7/23
 */
data class Credential(
    val id: CredentialId,
    val credentialValue: CredentialValue,
    val type: CredentialType = CredentialType.PASSWORD
) {
    constructor(id: CredentialId, credentialValue: String) : this(
        id,
        CredentialValue(credentialValue),
        CredentialType.PASSWORD,
    )

    companion object {
        const val MIN_LENGTH = 8
        private val charNumbers = '0'..'9'
        private val charUppercase = 'A'..'Z'
        private val charLowercase = 'a'..'z'
        private val charSpecial = "!@#$%^&*()_+{}|:<>?".toList()

        // Updated charset to include special characters for better entropy
        private val charset = charLowercase + charUppercase + charNumbers + charSpecial

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
            val passwordChars = mutableListOf<Char>()

            // Ensure at least one of each required character type
            passwordChars.add(charNumbers.random())
            passwordChars.add(charUppercase.random())
            passwordChars.add(charLowercase.random())
            passwordChars.add(charSpecial.random())

            // Fill the rest of the password with random characters from the full charset (including special chars)
            val remainingLength =
                MIN_LENGTH + kotlin.random.Random.nextInt(MIN_LENGTH) - passwordChars.size
            repeat(remainingLength) {
                passwordChars.add(charset.random())
            }

            return passwordChars.shuffled().joinToString("")
        }

        /**
         * Creates a new credential with the given value and type
         * @param credentialValue the value of the credential
         * @param type the type of the credential (default is [CredentialType.PASSWORD])
         * @return the created credential with the given value and type and a random id generated with [UUID.randomUUID]
         * @see UUID.randomUUID for the id generation
         * @see CredentialId for the id type
         * @see Credential for the credential type
         * @see CredentialType for the available types
         */
        fun create(
            credentialValue: String,
            type: CredentialType = CredentialType.PASSWORD
        ): Credential =
            Credential(CredentialId(UUID.randomUUID()), CredentialValue(credentialValue), type)
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
 * Credential type representation in the domain layer of the application that is used to identify the type of credential
 * @see Credential for the credential type
 */
enum class CredentialType {
    PASSWORD, TOTP, SECRET
}

/**
 * Credential value representation in the domain layer of the application that is used to identify
 * the value of a credential
 * @see Credential for the credential type
 */
@JvmInline
value class CredentialValue(val value: String) {
    init {
        if (value.isBlank()) throw CredentialException("Credential value cannot be blank")
        if (value.length < MIN_CREDENTIAL_LENGTH) {
            throw CredentialException(
                "Credential value must be at least $MIN_CREDENTIAL_LENGTH characters",
            )
        }
        if (value.length > MAX_CREDENTIAL_LENGTH) {
            throw CredentialException(
                "Credential value cannot exceed $MAX_CREDENTIAL_LENGTH characters",
            )
        }
        if (!value.any { it in '0'..'9' }) {
            throw CredentialException(
                "The password must have at least one number",
            )
        }
        if (!value.any { it in 'A'..'Z' }) {
            throw CredentialException(
                "The password must have at least one uppercase character",
            )
        }
        if (!value.any { it in 'a'..'z' }) {
            throw CredentialException(
                "The password must have at least one lowercase character",
            )
        }
        if (!value.any {
                it in "!@#$%^&*()_+{}|:<>?"
            }
        ) {
            throw CredentialException("The password must have at least one special character")
        }
    }

    override fun toString(): String = value

    companion object {
        private const val MAX_CREDENTIAL_LENGTH = 128
        private const val MIN_CREDENTIAL_LENGTH = 8
    }
}
