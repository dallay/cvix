package com.cvix

import com.cvix.common.domain.vo.credential.Credential
import com.cvix.common.domain.vo.credential.CredentialId
import com.cvix.common.domain.vo.credential.CredentialValue
import java.util.UUID
import net.datafaker.Faker

/**
 * CredentialGenerator is a utility class for generating credentials.
 * @created 2/8/23
 */
object CredentialGenerator {
    private val faker = Faker()
    fun generate(password: String = generateValidPassword()): Credential =
        Credential(CredentialId(UUID.randomUUID()), CredentialValue(password))

    fun generateValidPassword(): String {
        // Generate a password with length between 8 and 100 characters
        // Per OWASP guidance, we only enforce length, not composition rules
        return faker.credentials().password(8, 80, true, true, true)
    }
}
