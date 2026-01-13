package com.cvix.buildlogic.springboot

import com.cvix.buildlogic.common.ConventionPlugin
import com.cvix.buildlogic.common.extensions.catalogBundle
import com.cvix.buildlogic.common.extensions.catalogPlugin
import com.cvix.buildlogic.common.extensions.commonExtensions
import com.cvix.buildlogic.common.extensions.commonTasks
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

private const val TEST_IMPLEMENTATION = "testImplementation"

private const val IMPLEMENTATION = "implementation"

@Suppress("unused")
class SpringBootConventionPlugin : ConventionPlugin {
    override fun Project.configure() {
        apply(plugin = catalogPlugin("kotlin-jvm").get().pluginId)
        apply(plugin = catalogPlugin("kotlin-spring").get().pluginId)
        apply(plugin = catalogPlugin("spring-boot").get().pluginId)
        apply(plugin = catalogPlugin("spring-dependency-management").get().pluginId)

        with(extensions) {
            commonExtensions()
        }

        tasks.commonTasks()

        dependencies {
            add(IMPLEMENTATION, catalogBundle("spring-boot"))

            add(IMPLEMENTATION, catalogBundle("kotlin-jvm"))
            add(IMPLEMENTATION, catalogBundle("jackson"))

            add("developmentOnly", catalogBundle("spring-boot-dev"))

            add("annotationProcessor", "org.springframework.boot:spring-boot-configuration-processor")

            add(TEST_IMPLEMENTATION, catalogBundle("spring-boot-test"))

            add(TEST_IMPLEMENTATION, catalogBundle("tetscontainers"))

            add(TEST_IMPLEMENTATION, catalogBundle("jjwt"))

            add(TEST_IMPLEMENTATION, catalogBundle("kotest"))
        }
    }
}
