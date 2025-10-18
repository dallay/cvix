package com.loomify.buildlogic.library

import com.loomify.buildlogic.common.ConventionPlugin
import com.loomify.buildlogic.common.extensions.catalogBundle
import com.loomify.buildlogic.common.extensions.catalogPlugin
import com.loomify.buildlogic.common.extensions.commonExtensions
import com.loomify.buildlogic.common.extensions.commonTasks
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class LibraryConventionPlugin : ConventionPlugin {
    override fun Project.configure() {
        apply(plugin = catalogPlugin("kotlin-jvm").get().pluginId)

        with(extensions) {
            commonExtensions()
        }

        tasks.commonTasks()

        dependencies {

            add("implementation", catalogBundle("kotlin-jvm"))
            add("implementation", catalogBundle("jackson"))
        }
    }
}
