package com.loomify.engine.ratelimit.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class to enable rate limiting properties and create necessary beans.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties::class)
class RateLimitConfiguration {

    /**
     * Creates a BucketConfigurationStrategy bean that can be injected into other components.
     */
    @Bean
    fun bucketConfigurationStrategy(properties: RateLimitProperties): BucketConfigurationStrategy {
        return BucketConfigurationStrategy(properties)
    }
}

