package com.cvix.buildlogic.common

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KtJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KtVersion

object AppConfiguration {
    const val APP_NAME = "cvix"
    const val PACKAGE_NAME = "com.cvix"

    val useJavaVersion = JavaVersion.VERSION_25
    // Kotlin JVM target: Current compiler does not yet support JVM target 25
    // Using JVM_24 as fallback; bump to JVM_25 when supported (check gradle/libs.versions.toml)
    val jvmTarget = KtJvmTarget.JVM_24
    val jvmTargetStr = jvmTarget.target
    val kotlinVersion = KtVersion.KOTLIN_1_9

    // Detekt JVM target - keep at 22 until Detekt 2.0.0+ is stable (see gradle/libs.versions.toml)
    // Detekt only analyzes code (doesn't generate bytecode), so using older target is safe
    // Update to AppConfiguration.jvmTargetStr once Detekt supports JVM target 24+
    const val DETEKT_JVM_TARGET = "22"
}
