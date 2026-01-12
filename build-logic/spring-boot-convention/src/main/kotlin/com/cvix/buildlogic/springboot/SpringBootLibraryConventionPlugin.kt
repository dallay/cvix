package com.cvix.buildlogic.springboot

import com.cvix.buildlogic.common.ConventionPlugin
import com.cvix.buildlogic.common.extensions.catalogBundle
import com.cvix.buildlogic.common.extensions.catalogPlugin
import com.cvix.buildlogic.common.extensions.commonExtensions
import com.cvix.buildlogic.common.extensions.commonTasks
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

private const val IMPLEMENTATION = "implementation"

@Suppress("unused")
class SpringBootLibraryConventionPlugin : ConventionPlugin {
    override fun Project.configure() {
        apply(plugin = catalogPlugin("kotlin-jvm").get().pluginId)
        apply(plugin = catalogPlugin("kotlin-spring").get().pluginId)

        with(extensions) {
            commonExtensions()
        }

        tasks.commonTasks()

        dependencies {
            add(IMPLEMENTATION, catalogBundle("spring-boot"))

            add(IMPLEMENTATION, catalogBundle("kotlin-jvm"))

            add("testImplementation", catalogBundle("spring-boot-test"))
        }
    }
}
