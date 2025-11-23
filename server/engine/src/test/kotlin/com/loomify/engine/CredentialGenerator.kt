package com.loomify.engine

import com.loomify.common.domain.vo.credential.Credential
import com.loomify.common.domain.vo.credential.CredentialId
import com.loomify.common.domain.vo.credential.CredentialValue
import java.util.*
import net.datafaker.Faker

/**
 * CredentialGenerator is a utility class for generating credentials.
 * @created 2/8/23
 */
object CredentialGenerator {
    private val faker = Faker()
    fun generate(password: String = generateValidPassword()): Credential =
        Credential(CredentialId(UUID.randomUUID()), CredentialValue(password))

    fun generateValidPassword(maxAttempts: Int = 10): String {
        var password: String
        var attempts = 0

        do {
            password = faker.credentials().password(8, 80, true, true, true)
            if (isPasswordValid(password)) {
                return password
            }
            attempts++
        } while (attempts < maxAttempts)

        return "DefaultPass123!"
    }
    private fun isPasswordValid(password: String): Boolean {
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasLowercase && hasUppercase && hasDigit && hasSpecial
    }
}
