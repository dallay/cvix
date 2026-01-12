package com.cvix.spring.boot.presentation.filter

import com.cvix.common.domain.presentation.filter.RHSFilterParser
import tools.jackson.databind.ObjectMapper
import kotlin.reflect.KClass
import org.springframework.stereotype.Component

@Component
class RHSFilterParserFactory(
    private val objectMapper: ObjectMapper,
) {
    fun <T : Any> create(clazz: KClass<T>): RHSFilterParser<T> = RHSFilterParser(clazz, objectMapper)
}

inline fun <reified T : Any> RHSFilterParserFactory.create(): RHSFilterParser<T> =
    this.create(T::class)
