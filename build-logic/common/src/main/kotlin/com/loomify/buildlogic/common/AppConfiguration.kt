package com.loomify.buildlogic.common

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KtJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KtVersion

object AppConfiguration {
    const val appName = "loomify"
    const val packageName = "com.loomify"

    val useJavaVersion = JavaVersion.toVersion("25")
    // The Kotlin compiler does not yet support a JVM target of 25.
    // We are setting the JVM target to 21 to ensure compatibility.
    // The project will still be built with Java 25 and run on the Java 25 runtime.
    val jvmTarget = KtJvmTarget.fromTarget("21")
    val jvmTargetStr = jvmTarget.target
    val kotlinVersion = KtVersion.KOTLIN_2_1
}
