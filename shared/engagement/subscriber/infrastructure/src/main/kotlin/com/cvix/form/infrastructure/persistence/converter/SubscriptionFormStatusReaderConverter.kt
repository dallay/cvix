package com.cvix.form.infrastructure.persistence.converter

import com.cvix.form.domain.SubscriptionFormStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter

/**
 * R2DBC converter to read [SubscriptionFormStatus] enum from the database.
 * The actual PostgreSQL enum type mapping is handled by EnumCodec at the ConnectionFactory level.
 */
@ReadingConverter
class SubscriptionFormStatusReaderConverter : Converter<String, SubscriptionFormStatus> {
    override fun convert(source: String): SubscriptionFormStatus =
        SubscriptionFormStatus.valueOf(source)
}
