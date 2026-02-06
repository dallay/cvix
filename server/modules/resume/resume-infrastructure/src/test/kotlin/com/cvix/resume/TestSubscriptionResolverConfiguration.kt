package com.cvix.resume

import com.cvix.subscription.domain.ResolverContext
import com.cvix.subscription.domain.SubscriptionResolver
import com.cvix.subscription.domain.SubscriptionTier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration(proxyBeanMethods = false)
class TestSubscriptionResolverConfiguration {
    @Bean
    fun subscriptionResolver(): SubscriptionResolver =
        SubscriptionResolver { _: ResolverContext -> SubscriptionTier.FREE }
}
