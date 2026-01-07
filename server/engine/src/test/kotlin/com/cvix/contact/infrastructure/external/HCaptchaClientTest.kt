package com.cvix.contact.infrastructure.external

import com.cvix.UnitTest
import com.cvix.contact.infrastructure.config.ContactProperties
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.lang.reflect.Method
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

@UnitTest
class HCaptchaClientTest {

    private val properties = ContactProperties(
        webhookUrl = "https://example.com/webhook",
        headerApiKey = "x-api-key",
        apiKey = "test-api-key",
        formTokenId = "test-form-token",
        hcaptchaSecretKey = "test-secret&key=special",
    )

    // Create a dedicated WebClient for testing (mimics hcaptchaWebClient bean)
    private val webClient = WebClient.builder()
        .baseUrl("https://hcaptcha.com")
        .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
        .build()

    private val client = HCaptchaClient(webClient, properties)

    @Test
    fun `should properly URL-encode parameters in verification body`() {
        // Arrange
        val token = "token&with=special+chars"
        val ipAddress = "192.168.1.1"

        // Use reflection to access private method for testing
        val buildMethod: Method = HCaptchaClient::class.java.getDeclaredMethod(
            "buildVerificationBody",
            String::class.java,
            String::class.java,
        )
        buildMethod.isAccessible = true

        // Act
        val body = buildMethod.invoke(client, token, ipAddress) as String

        // Assert - verify proper URL encoding
        body shouldContain "secret=test-secret%26key%3Dspecial" // & and = encoded
        body shouldContain "response=token%26with%3Dspecial%2Bchars" // &, =, and + encoded
        body shouldContain "remoteip=192.168.1.1"

        // Verify the body doesn't contain unencoded special characters
        body shouldNotContain "token&with=special+chars"
    }

    @Test
    fun `should create valid form-urlencoded body with simple values`() {
        // Arrange
        val token = "simple-token-123"
        val ipAddress = "127.0.0.1"

        // Use reflection to access private method
        val buildMethod: Method = HCaptchaClient::class.java.getDeclaredMethod(
            "buildVerificationBody",
            String::class.java,
            String::class.java,
        )
        buildMethod.isAccessible = true

        // Act
        val body = buildMethod.invoke(client, token, ipAddress) as String

        // Assert
        body shouldContain "secret="
        body shouldContain "response=simple-token-123"
        body shouldContain "remoteip=127.0.0.1"
        body shouldContain "&" // Parameters should be joined with &
    }

    @Test
    fun `should handle empty token gracefully`() {
        // Arrange
        val token = ""
        val ipAddress = "192.168.1.1"

        // Use reflection to access private method
        val buildMethod: Method = HCaptchaClient::class.java.getDeclaredMethod(
            "buildVerificationBody",
            String::class.java,
            String::class.java,
        )
        buildMethod.isAccessible = true

        // Act
        val body = buildMethod.invoke(client, token, ipAddress) as String

        // Assert
        body shouldContain "secret="
        body shouldContain "response=" // Empty but present
        body shouldContain "remoteip=192.168.1.1"
    }

    @Test
    fun `should encode IPv6 addresses correctly`() {
        // Arrange
        val token = "token"
        val ipv6Address = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"

        // Use reflection to access private method
        val buildMethod: Method = HCaptchaClient::class.java.getDeclaredMethod(
            "buildVerificationBody",
            String::class.java,
            String::class.java,
        )
        buildMethod.isAccessible = true

        // Act
        val body = buildMethod.invoke(client, token, ipv6Address) as String

        // Assert - IPv6 colons should be encoded
        body shouldContain "remoteip=2001%3A0db8%3A85a3%3A0000%3A0000%3A8a2e%3A0370%3A7334"
    }
}
