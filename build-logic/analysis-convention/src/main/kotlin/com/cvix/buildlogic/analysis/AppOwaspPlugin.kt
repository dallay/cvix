package com.cvix.buildlogic.analysis

import com.cvix.buildlogic.common.AppConfiguration.APP_NAME
import com.cvix.buildlogic.common.ConventionPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator

// see https://owasp.org/www-project-dependency-check/#what-is-a-cvss-score
private const val FAIL_BUILDS_ON_CVSS: Float = 11F // SET THIS TO A REASONABLE VALUE FOR YOUR PROJECT
private const val AUTO_UPDATE: Boolean = true // Enable auto-update of the NVD database
private const val PURGE_DATABASE: Boolean = true // Enable purging of the database to fix corruption issues

private const val DEFAULT_DELAY = 1000

@Suppress("unused")
internal class AppOwaspPlugin : ConventionPlugin {
    override fun Project.configure() {
        apply(plugin = "org.owasp.dependencycheck")

        // Register a task to purge the dependency check database
        tasks.register<Delete>("purgeDependencyCheckDatabase") {
            description = "Purges the dependency check database to fix corruption issues"
            group = "security"

            doFirst {
                println("Purging dependency check database...")
            }

            // Delete the database files in the dependency-check-data directory
            delete(
                fileTree(layout.buildDirectory.dir("dependency-check-data").get().asFile) {
                    include("*.h2.db")
                    include("*.mv.db") // Include odc.mv.db file
                    include("*.trace.db")
                    include("*.lock.db")
                },
            )

            doLast {
                println("Dependency check database purged successfully.")
            }
        }

        // Make dependencyCheckAnalyze task depend on purgeDependencyCheckDatabase if purging is enabled
        if (PURGE_DATABASE) {
            tasks.named("dependencyCheckAnalyze").configure {
                dependsOn("purgeDependencyCheckDatabase")
            }
        }

        with(extensions) {
            configure<DependencyCheckExtension> {
                failBuildOnCVSS.set(FAIL_BUILDS_ON_CVSS)
                formats.set(
                    listOf(
                        ReportGenerator.Format.HTML.toString(),
                        ReportGenerator.Format.JUNIT.toString(),
                        ReportGenerator.Format.XML.toString(),
                        ReportGenerator.Format.SARIF.toString(),
                    ),
                )
                suppressionFile.set("${rootProject.rootDir}/config/owasp/owasp-suppression.xml")

                setEnvironmentVariables()

                // Configure the data directory to store the NVD data and the H2 database
                data {
                    directory.set(layout.buildDirectory.dir("dependency-check-data").get().asFile.absolutePath)
                }

                // Enable auto-update of the NVD database
                autoUpdate.set(AUTO_UPDATE)

                // remove plugin dependencies, for configs see
                // https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_plugin_and_dependency_management
                val validConfigurations = listOf("compileClasspath", "runtimeClasspath", "default")
                scanConfigurations.set(
                    configurations.names
                        .filter { validConfigurations.contains(it) }
                        .toList(),
                )
                outputDirectory.set(layout.buildDirectory.dir("reports/owasp").get())
            }
        }
    }

    private fun DependencyCheckExtension.setEnvironmentVariables() {
        val apiKeyValue = System.getenv("NVD_API_KEY")
        if (apiKeyValue != null) {
            nvd {
                apiKey.set(apiKeyValue)
            }
            println("✅ NVD_API_KEY loaded from environment.")
            println("   This increases rate limits when querying the NVD.")
            println("   See: https://nvd.nist.gov/vuln/data-feeds#apikey")
            println("   Tip: add the key as a secret in your CI/CD")
            println("   or export NVD_API_KEY in your local environment. ($APP_NAME)")
        } else {
            println("⚠️ NVD_API_KEY not found in the environment.")
            println("   NVD queries may be rate-limited.")
            println("   Create an API key at: https://nvd.nist.gov/vuln/data-feeds#apikey")
            println("   Options: export NVD_API_KEY=... or add it as a secret in your CI/CD.")
            println("   ($APP_NAME)")
        }
        val delayValue = System.getenv("NVD_API_DELAY")
        if (delayValue != null) {
            val delayInt = delayValue.toIntOrNull()
            if (delayInt != null) {
                if (delayInt <= 0) {
                    println("⚠️ [NVD_API_DELAY] must be positive. Got: $delayInt ms. Defaulting to ${DEFAULT_DELAY}ms.")
                    nvd {
                        delay.set(DEFAULT_DELAY)
                    }
                } else {
                    nvd {
                        delay.set(delayInt)
                    }
                    println("✅ [NVD_API_DELAY] loaded from environment: $delayInt ms.")
                }
            } else {
                println("⚠️ [NVD_API_DELAY] is set but is not a valid integer: '$delayValue'.")
                println("   Defaulting to ${DEFAULT_DELAY}ms.")
                nvd {
                    delay.set(DEFAULT_DELAY)
                }
            }
        } else {
            println("⚠️ [NVD_API_DELAY] not found in the environment. Defaulting to ${DEFAULT_DELAY}ms.")
            nvd {
                delay.set(DEFAULT_DELAY)
            }
        }
    }
}
