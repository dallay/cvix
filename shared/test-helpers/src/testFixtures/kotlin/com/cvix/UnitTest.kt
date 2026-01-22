package com.cvix

import java.lang.annotation.Inherited
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.Tag
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@DisplayNameGeneration(com.cvix.ReplaceCamelCase::class)
@Inherited
@Tag("unit")
@ActiveProfiles("test")
annotation class UnitTest
