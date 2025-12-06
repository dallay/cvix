package com.cvix.buildlogic.common

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KtJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KtVersion

object AppConfiguration {
    const val APP_NAME = "cvix"
    const val PACKAGE_NAME = "com.cvix"

    val useJavaVersion = JavaVersion.VERSION_25
    // Kotlin 2.2.21 does not support JVM target 25 yet, fallback to JVM 24
    val jvmTarget = KtJvmTarget.JVM_24
    val jvmTargetStr = jvmTarget.target
    val kotlinVersion = KtVersion.KOTLIN_1_9
}
