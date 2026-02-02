package com.cvix.form.infrastructure.persistence.converter

import com.cvix.form.domain.SubscriptionFormStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

/**
 * R2DBC converter to write [SubscriptionFormStatus] enum to the database.
 * The actual PostgreSQL enum type mapping is handled by EnumCodec at the ConnectionFactory level.
 */
@WritingConverter
class SubscriptionFormStatusWriterConverter : Converter<SubscriptionFormStatus, SubscriptionFormStatus> {
    override fun convert(source: SubscriptionFormStatus): SubscriptionFormStatus = source
}
