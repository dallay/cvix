package com.cvix.config

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.datatype.jsr310.JavaTimeModule
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.ProblemDetail
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.converter.json.ProblemDetailJacksonMixin
import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 * Jackson configuration for WebFlux.
 *
 * IMPORTANT: When @EnableWebFlux is used, Spring Boot's auto-configuration for Jackson
 * (spring.jackson.* properties in application.yml) is NOT automatically applied.
 * This class explicitly configures Jackson for WebFlux to ensure consistent
 * date serialization as ISO-8601 strings instead of arrays.
 *
 * Without this configuration, LocalDate would serialize as [2018, 9, 5] instead of "2018-09-05"
 * causing frontend deserialization issues.
 */
@Configuration
class JacksonConfig : WebFluxConfigurer {

    // Lazy-initialized ObjectMapper to avoid duplicate instantiation
    // in configureHttpMessageCodecs
    private val mapper: ObjectMapper by lazy { objectMapper() }

    /**
     * Primary ObjectMapper bean used throughout the application.
     * Configured to serialize dates as ISO-8601 strings.
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = jacksonObjectMapper().apply {
        // Register Java 8 date/time module
        registerModule(JavaTimeModule())
        // Discover and register other modules (like Kotlin module)
        findAndRegisterModules()

        // CRITICAL: Serialize dates as ISO-8601 strings, NOT as arrays
        // Without this, LocalDate serializes as [2018, 9, 5] instead of "2018-09-05"
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        // Don't fail on unknown properties during deserialization
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // CRITICAL: Register Spring's ProblemDetail mixin for proper error response serialization.
        // This mixin uses @JsonAnyGetter/@JsonAnySetter to serialize custom properties
        // (errorCategory, timestamp, traceId, etc.) as top-level JSON fields.
        // Without this, ProblemDetail.setProperty() values would not appear in the response.
        addMixIn(ProblemDetail::class.java, ProblemDetailJacksonMixin::class.java)
    }

    /**
     * Configure WebFlux HTTP message codecs to use our customized ObjectMapper.
     * This ensures all HTTP request/response serialization uses the correct date format.
     */
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(mapper))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(mapper))
    }
}
