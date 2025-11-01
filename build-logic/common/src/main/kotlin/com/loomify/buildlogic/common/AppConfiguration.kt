package com.loomify.buildlogic.common

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KtJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KtVersion

object AppConfiguration {
    const val appName = "loomify"
    const val packageName = "com.loomify"

    val useJavaVersion = JavaVersion.VERSION_25
    val jvmTarget = KtJvmTarget.JVM_24 // Kotlin 2.2.x supports up to JVM 24 bytecode
    const val jvmTargetStr = "25" // Use Java 25 for toolchain
    val kotlinVersion = KtVersion.KOTLIN_2_0 // API version 2.0 is latest supported
}
