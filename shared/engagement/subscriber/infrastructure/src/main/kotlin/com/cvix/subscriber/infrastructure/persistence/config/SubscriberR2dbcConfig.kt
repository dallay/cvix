package com.cvix.subscriber.infrastructure.persistence.config

import com.cvix.form.domain.SubscriptionFormStatus
import com.cvix.form.infrastructure.persistence.converter.SubscriptionFormStatusReaderConverter
import com.cvix.form.infrastructure.persistence.converter.SubscriptionFormStatusWriterConverter
import com.cvix.subscriber.infrastructure.persistence.converter.SubscriberAttributesReaderConverter
import com.cvix.subscriber.infrastructure.persistence.converter.SubscriberAttributesWriterConverter
import com.cvix.subscriber.infrastructure.persistence.converter.SubscriberStatusWriterConverter
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.postgresql.extension.Extension
import org.springframework.boot.r2dbc.autoconfigure.ConnectionFactoryOptionsBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import tools.jackson.databind.json.JsonMapper

@Configuration
class SubscriberR2dbcConfig {

    /**
     * Customizes the R2DBC ConnectionFactory to register the EnumCodec for
     * PostgreSQL enum types. This is required to properly map Java enums
     * to native PostgreSQL enum types.
     */
    @Bean
    fun connectionFactoryOptionsBuilderCustomizer(): ConnectionFactoryOptionsBuilderCustomizer {
        return ConnectionFactoryOptionsBuilderCustomizer { builder ->
            builder.option(
                PostgresqlConnectionFactoryProvider.EXTENSIONS,
                mutableListOf<Extension>(
                    EnumCodec.builder()
                        .withEnum("subscription_form_status", SubscriptionFormStatus::class.java)
                        .build(),
                ),
            )
        }
    }

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters: MutableList<Converter<*, *>> = ArrayList()

        // Use a shared JsonMapper instance for converters
        val jsonMapper = JsonMapper()

        // Use project-provided converters for enum and attributes conversion
        converters.add(SubscriberAttributesWriterConverter(jsonMapper))
        converters.add(SubscriberAttributesReaderConverter(jsonMapper))
        converters.add(SubscriberStatusWriterConverter())
        converters.add(SubscriptionFormStatusWriterConverter())
        converters.add(SubscriptionFormStatusReaderConverter())

        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters)
    }
}
