package com.cvix.common.util

/**
 * Utility object for accessing system environment variables in a safe and consistent way.
 *
 * Provides methods to retrieve environment variables with support for default values.
 *
 * @created 2026-01-08
 */
object SystemEnvironment {
    /**
     * Retrieves the value of the specified environment variable.
     *
     * @param key The name of the environment variable to retrieve.
     * @param default The value to return if the environment variable is not set.
     * @return The value of the environment variable, or [default] if not present.
     */
    fun getEnvOrDefault(key: String, default: String): String =
        System.getenv(key) ?: default
}
