package com.cvix.buildlogic.common

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KtJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KtVersion

object AppConfiguration {
    const val APP_NAME = "cvix"
    const val PACKAGE_NAME = "com.cvix"

    val useJavaVersion = JavaVersion.VERSION_21
    val jvmTarget = KtJvmTarget.fromTarget(useJavaVersion.toString())
    val jvmTargetStr = jvmTarget.target
    val kotlinVersion = KtVersion.KOTLIN_1_9
}
