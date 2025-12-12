package com.cvix.resume.infrastructure.template.metadata

import com.cvix.UnitTest
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Test

@UnitTest
class YamlTemplateMetadataLoaderTest {

    private val loader = YamlTemplateMetadataLoader()

    @Test
    fun `should parse requiredSubscriptionTier as BASIC from YAML`() {
        val yaml = """
            id: simple
            name: Simple Resume
            version: 1.0.0
            templatePath: classpath:templates/resume/simple/simple.stg
            requiredSubscriptionTier: BASIC
        """.trimIndent()

        val metadata = kotlinx.coroutines.runBlocking {
            loader.loadTemplateMetadata(
                ByteArrayInputStream(yaml.toByteArray()),
                "test-metadata.yaml",
            )
        }

        metadata.id shouldBe "simple"
        metadata.name shouldBe "Simple Resume"
        metadata.requiredSubscriptionTier shouldBe SubscriptionTier.BASIC
    }

    @Test
    fun `should default to FREE tier when requiredSubscriptionTier is not specified`() {
        val yaml = """
            id: simple
            name: Simple Resume
            version: 1.0.0
            templatePath: classpath:templates/resume/simple/simple.stg
        """.trimIndent()

        val metadata = kotlinx.coroutines.runBlocking {
            loader.loadTemplateMetadata(
                ByteArrayInputStream(yaml.toByteArray()),
                "test-metadata.yaml",
            )
        }

        metadata.requiredSubscriptionTier shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `should parse PROFESSIONAL tier from YAML`() {
        val yaml = """
            id: premium
            name: Premium Resume
            version: 1.0.0
            templatePath: classpath:templates/resume/premium/premium.stg
            requiredSubscriptionTier: PROFESSIONAL
        """.trimIndent()

        val metadata = kotlinx.coroutines.runBlocking {
            loader.loadTemplateMetadata(
                ByteArrayInputStream(yaml.toByteArray()),
                "test-metadata.yaml",
            )
        }

        metadata.requiredSubscriptionTier shouldBe SubscriptionTier.PROFESSIONAL
    }

    @Test
    fun `should handle case-insensitive tier names`() {
        val yaml = """
            id: test
            name: Test Resume
            version: 1.0.0
            templatePath: classpath:templates/resume/test/test.stg
            requiredSubscriptionTier: basic
        """.trimIndent()

        val metadata = kotlinx.coroutines.runBlocking {
            loader.loadTemplateMetadata(
                ByteArrayInputStream(yaml.toByteArray()),
                "test-metadata.yaml",
            )
        }

        metadata.requiredSubscriptionTier shouldBe SubscriptionTier.BASIC
    }

    @Test
    fun `should default to FREE for invalid tier values`() {
        val yaml = """
            id: test
            name: Test Resume
            version: 1.0.0
            templatePath: classpath:templates/resume/test/test.stg
            requiredSubscriptionTier: INVALID_TIER
        """.trimIndent()

        val metadata = kotlinx.coroutines.runBlocking {
            loader.loadTemplateMetadata(
                ByteArrayInputStream(yaml.toByteArray()),
                "test-metadata.yaml",
            )
        }

        metadata.requiredSubscriptionTier shouldBe SubscriptionTier.FREE
    }
}
