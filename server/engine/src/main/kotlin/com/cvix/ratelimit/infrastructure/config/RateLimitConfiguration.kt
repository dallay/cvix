package com.cvix.ratelimit.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class to enable rate limiting properties and create necessary beans.
 *
 * @since 2.0.0
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties::class)
class RateLimitConfiguration {

    /**
     * Creates a BucketConfigurationFactory bean that can be injected into other components.
     */
    @Bean
    fun bucketConfigurationFactory(properties: RateLimitProperties): BucketConfigurationFactory =
        BucketConfigurationFactory(properties)
}
