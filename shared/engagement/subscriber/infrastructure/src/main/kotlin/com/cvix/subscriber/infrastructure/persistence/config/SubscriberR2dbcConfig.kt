package com.cvix.subscriber.infrastructure.persistence.config

import com.cvix.subscriber.infrastructure.persistence.converter.SubscriberAttributesReaderConverter
import com.cvix.subscriber.infrastructure.persistence.converter.SubscriberAttributesWriterConverter
import com.cvix.subscriber.infrastructure.persistence.converter.SubscriberStatusWriterConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions

@Configuration
class SubscriberR2dbcConfig {

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters: MutableList<Any> = ArrayList()
        converters.add(SubscriberAttributesWriterConverter(tools.jackson.databind.json.JsonMapper()))
        converters.add(SubscriberAttributesReaderConverter(tools.jackson.databind.json.JsonMapper()))
        converters.add(SubscriberStatusWriterConverter())

        return R2dbcCustomConversions(R2dbcCustomConversions.STORE_CONVERSIONS, converters)
    }
}
