package com.cvix.buildlogic.analysis

import com.cvix.buildlogic.common.ConventionPlugin
import com.cvix.buildlogic.common.extensions.catalogLib
import com.cvix.buildlogic.common.extensions.detekt
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register

@Suppress("unused")
internal class AppDetektPlugin : ConventionPlugin {
    override fun Project.configure() {
        apply(plugin = "io.gitlab.arturbosch.detekt")

        tasks.register<Detekt>("detektAll") {
            description = "Run detekt in all modules"

            parallel = true
            ignoreFailures = false
            autoCorrect = true
            buildUponDefaultConfig = true
            // Note: Detekt 1.23.8 doesn't support JVM target 24 yet
            // Using JVM 22 (maximum supported) is safe as Detekt only analyzes code
            // When Detekt 2.0.0 stable is released, we can upgrade to use JVM target 24
            jvmTarget = "22"
            setSource(
                fileTree(projectDir).matching {
                    include("**/*.kt", "**/*.kts")
                }.files,
            )
            config.setFrom(files("$rootDir/config/detekt.yml"))
            include("**/*.kt", "**/*.kts")
            exclude("**/resources/**", "**/build/**")

            reports {
                html.required.set(true)
                sarif.required.set(true)
                txt.required.set(false)
                xml.required.set(true)
            }
        }

        dependencies {
            detekt(catalogLib("detekt-compose"))
            detekt(catalogLib("detekt-compose2"))
            detekt(catalogLib("detekt-formatting"))
        }
    }
}
