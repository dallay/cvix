package com.cvix.resume.domain

/**
 * Defines the supported template source types for resume rendering.
 *
 * Each enum entry exposes a `key` that is used for component identification,
 * wiring strategies, and dynamic template resolution across the system.
 */
enum class TemplateSourceType(val key: String) {
    CLASSPATH(TemplateSourceKeys.CLASSPATH),
    FILESYSTEM(TemplateSourceKeys.FILESYSTEM)
}

/**
 * Centralized registry for template source identifier keys.
 *
 * These constants act as the single source of truth to avoid duplicated
 * string literals across the application.
 */
object TemplateSourceKeys {
    const val CLASSPATH = "classpath"
    const val FILESYSTEM = "filesystem"
}
