package com.cvix.buildlogic.library

import com.cvix.buildlogic.common.ConventionPlugin
import com.cvix.buildlogic.common.extensions.catalogBundle
import com.cvix.buildlogic.common.extensions.catalogPlugin
import com.cvix.buildlogic.common.extensions.commonExtensions
import com.cvix.buildlogic.common.extensions.commonTasks
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class LibraryConventionPlugin : ConventionPlugin {
    override fun Project.configure() {
        apply(plugin = catalogPlugin("kotlin-jvm").get().pluginId)

        with(extensions) {
            commonExtensions()
        }

        tasks.commonTasks()

        dependencies {

            add("implementation", catalogBundle("kotlin-jvm"))
        }
    }
}
