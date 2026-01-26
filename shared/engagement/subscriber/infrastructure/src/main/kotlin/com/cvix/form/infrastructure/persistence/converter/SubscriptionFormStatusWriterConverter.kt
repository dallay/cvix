package com.cvix.form.infrastructure.persistence.converter

import com.cvix.form.domain.SubscriptionFormStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

/**
 * R2DBC converter to write [SubscriptionFormStatus] enum to the database.
 */
@WritingConverter
class SubscriptionFormStatusWriterConverter : Converter<SubscriptionFormStatus, String> {
    override fun convert(source: SubscriptionFormStatus): String = source.name
}
