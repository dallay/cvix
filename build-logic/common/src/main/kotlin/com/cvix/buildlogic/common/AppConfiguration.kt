package com.cvix.buildlogic.common

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KtJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KtVersion

object AppConfiguration {
    const val APP_NAME = "cvix"
    const val PACKAGE_NAME = "com.cvix"

    val useJavaVersion = JavaVersion.VERSION_21
    // Kotlin JVM target: Using JVM_21 for Spring Boot 4.0.1 compatibility (LTS version)
    val jvmTarget = KtJvmTarget.JVM_21
    val jvmTargetStr = jvmTarget.target
    val kotlinVersion = KtVersion.KOTLIN_2_0

    // Detekt JVM target - keep at 21 for better compatibility
    const val DETEKT_JVM_TARGET = "21"
}
