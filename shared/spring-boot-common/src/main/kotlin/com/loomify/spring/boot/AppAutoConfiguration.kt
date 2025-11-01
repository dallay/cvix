package com.loomify.spring.boot

import com.loomify.common.domain.bus.Mediator
import com.loomify.common.domain.bus.MediatorBuilder
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
class AppAutoConfiguration {
    @Bean
    fun appSpringBeanProvider(applicationContext: ApplicationContext): AppSpringBeanProvider =
        AppSpringBeanProvider(applicationContext)

    @Bean
    fun mediator(appSpringBeanProvider: AppSpringBeanProvider): Mediator =
        MediatorBuilder(appSpringBeanProvider).build()
}
