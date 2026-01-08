package com.cvix.common.domain

/**
 *
 * @created 8/1/26
 */
object SystemEnvironment {
    /**
     * Retrieves environment variable or returns default value
     */
    fun getEnvOrDefault(key: String, default: String): String =
        System.getenv(key) ?: default
}
