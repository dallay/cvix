package com.loomify.buildlogic.springboot

import com.loomify.buildlogic.common.ConventionPlugin
import com.loomify.buildlogic.common.extensions.catalogBundle
import com.loomify.buildlogic.common.extensions.catalogPlugin
import com.loomify.buildlogic.common.extensions.commonExtensions
import com.loomify.buildlogic.common.extensions.commonTasks
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

private const val IMPLEMENTATION = "implementation"

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

            add(IMPLEMENTATION, catalogBundle("jackson"))

            add("testImplementation", catalogBundle("spring-boot-test"))
        }
    }
}
