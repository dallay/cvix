package com.cvix.form.infrastructure.persistence.converter

import com.cvix.UnitTest
import com.cvix.form.domain.SubscriptionFormStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriptionFormStatusConverterTest {

    private val writer = SubscriptionFormStatusWriterConverter()

    @Test
    fun `writer should return the enum instance itself for driver handling`() {
        val value = SubscriptionFormStatus.PUBLISHED
        val result = writer.convert(value)
        assertEquals(SubscriptionFormStatus.PUBLISHED, result)
    }
}
