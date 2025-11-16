package com.loomify.buildlogic.gradle

import com.loomify.buildlogic.common.ConventionPlugin
import com.loomify.buildlogic.common.extensions.isRelease
import com.loomify.buildlogic.common.extensions.kover
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

private const val DEFAULT_KOVER_MIN_COVERAGE = 80
private const val KOVER_MIN_COVERAGE_LOWER_BOUND = 0
private const val KOVER_MIN_COVERAGE_UPPER_BOUND = 100

private fun Project.getMinCoverageBound(): Int =
    (findProperty("koverMinCoverage") as? String)?.toIntOrNull()
        ?.coerceIn(KOVER_MIN_COVERAGE_LOWER_BOUND, KOVER_MIN_COVERAGE_UPPER_BOUND)
        ?: DEFAULT_KOVER_MIN_COVERAGE

@Suppress("unused")
internal class AppKoverPlugin : ConventionPlugin {
    private val classesExcludes = listOf(
        // Serializers
        "*.*$\$serializer",
    )
    private val packagesIncludes = listOf("com.loomify")
    private val packagesExcludes = listOf(
        // Common
        "*.buildlogic.*",
        "*.mock.*",
        "*.aop.*",
    )
    private val containerModules = listOf<String>()

    override fun Project.configure() {
        allprojects {
            if (path !in containerModules) {
                apply(plugin = "org.jetbrains.kotlinx.kover")
            }
        }

        with(extensions) {
            configure<KoverProjectExtension> { configure(project) }
        }

        dependencies {
            subprojects
                .filterNot { it.path in containerModules }
                .forEach { kover(it.path) }
        }
    }

    private fun KoverProjectExtension.configure(project: Project) {
        project.tasks.withType<Test> {
            if (isRelease) disable()
        }

        reports {
            filters {
                includes { packages(packagesIncludes) }
                excludes {
                    annotatedBy(
                        "androidx.compose.runtime.Composable",
                        "androidx.compose.ui.tooling.preview.Preview",
                        "com.loomify.common.domain.Generated",
                    )
                    classes(classesExcludes)
                    packages(packagesExcludes)
                    annotatedBy("*Generated*")
                }
            }
            verify {
                rule {
                    minBound(project.getMinCoverageBound())
                }
            }
        }
    }
}
