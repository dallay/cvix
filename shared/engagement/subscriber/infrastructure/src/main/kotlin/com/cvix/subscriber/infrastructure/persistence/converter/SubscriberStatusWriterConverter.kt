package com.cvix.subscriber.infrastructure.persistence.converter

import com.cvix.subscriber.domain.SubscriberStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

/**
 * R2DBC converter to write [SubscriberStatus] enum to the database.
 * Spring Data R2DBC usually handles enums as Strings by default, but this
 * ensures consistency if the database expects a specific format.
 */
@WritingConverter
class SubscriberStatusWriterConverter : Converter<SubscriberStatus, String> {
    override fun convert(source: SubscriberStatus): String = source.name
}
