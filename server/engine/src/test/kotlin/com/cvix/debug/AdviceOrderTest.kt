package com.cvix.debug

import org.junit.jupiter.api.Test
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.annotation.Order
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestControllerAdvice

class AdviceOrderTest {

    @Test
    fun `print controller advice beans and order`() {
        // Keep this test fast and isolated: don't bootstrap full application context
        // Instead scan the classpath for @ControllerAdvice / @RestControllerAdvice types and
        // inspect their @Order value reflectively. This avoids initializing unrelated beans.
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(ControllerAdvice::class.java))
        scanner.addIncludeFilter(AnnotationTypeFilter(RestControllerAdvice::class.java))

        val basePackage = "com.cvix"
        val candidates = try {
            scanner.findCandidateComponents(basePackage)
        } catch (e: Exception) {
            println("Could not scan package $basePackage: ${e.message}")
            return
        }

        println("---- ControllerAdvice classes ----")
        // avoid labeled returns (detekt: LabeledExpression) by using a simple for-loop
        for (beanDef in candidates) {
            val className = beanDef.beanClassName
            if (className == null) continue
            try {
                val cls = Class.forName(className)
                val order = cls.getAnnotation(Order::class.java)?.value ?: "none"
                println("$className order=$order")
            } catch (e: Exception) {
                println("Could not load class $className: ${e.message}")
            }
        }
    }
}
