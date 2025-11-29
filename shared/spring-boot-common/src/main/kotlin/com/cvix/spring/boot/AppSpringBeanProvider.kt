@file:Suppress("UNCHECKED_CAST")

package com.cvix.spring.boot

import com.cvix.common.domain.bus.DependencyProvider
import org.springframework.context.ApplicationContext

class AppSpringBeanProvider(
    private val applicationContext: ApplicationContext,
) : DependencyProvider {
    override fun <T> getSingleInstanceOf(clazz: Class<T>): T = applicationContext.getBean(clazz)

    override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> =
        applicationContext.getBeanNamesForType(clazz)
            .map { applicationContext.getType(it) as Class<T> }
}
