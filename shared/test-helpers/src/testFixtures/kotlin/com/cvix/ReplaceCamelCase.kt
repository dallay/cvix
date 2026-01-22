package com.cvix

import java.lang.reflect.Method
import java.util.*
import org.junit.jupiter.api.DisplayNameGenerator

class ReplaceCamelCase : DisplayNameGenerator.Standard() {
    /**
     * Generates a display name for a test method by replacing capital letters and numbers
     * in the method name with spaces, and converting the entire name to lowercase.
     * Example: "testUserLogin2" becomes "test user login 2"
     * @param enclosingInstanceTypes the list of enclosing instance types
     * @param testClass the test class
     * @param testMethod the test method
     * @return the generated display name
     */
    override fun generateDisplayNameForMethod(
        enclosingInstanceTypes: List<Class<*>>,
        testClass: Class<*>,
        testMethod: Method
    ): String = replaceCapitals(testMethod.name)

    private fun replaceCapitals(originalName: String): String =
        originalName.replace("([A-Z])".toRegex(), " $1")
            .replace("([0-9]+)".toRegex(), " $1")
            .lowercase(Locale.getDefault())
}
