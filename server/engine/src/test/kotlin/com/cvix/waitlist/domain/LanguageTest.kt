package com.cvix.waitlist.domain

import com.cvix.common.domain.model.Language
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class LanguageTest {
    @Test
    fun `fromString returns correct Language for valid codes`() {
        Language.fromString("en") shouldBe Language.ENGLISH
        Language.fromString("es") shouldBe Language.SPANISH
        // Test case-insensitive lookup
        Language.fromString("EN") shouldBe Language.ENGLISH
        Language.fromString("Es") shouldBe Language.SPANISH
    }

    @Test
    fun `fromString throws IllegalArgumentException for invalid codes`() {
        shouldThrow<IllegalArgumentException> { Language.fromString("fr") }
        shouldThrow<IllegalArgumentException> { Language.fromString("") }
        shouldThrow<IllegalArgumentException> { Language.fromString("foo") }
        shouldThrow<IllegalArgumentException> { Language.fromString(null) }
    }
}
