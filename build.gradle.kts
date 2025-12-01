plugins {
    idea
    eclipse
    alias(libs.plugins.spring.boot).apply(false)
    alias(libs.plugins.spring.dependency.management).apply(false)
    kotlin("jvm").version(libs.versions.kotlin).apply(false)
    kotlin("plugin.spring").version(libs.versions.kotlin).apply(false)
    id("app.dependency-versions")
    id("app.detekt")
    id("app.kover")
    id("app.owasp.dependency.check")
}
group = rootProject.findProperty("group")?.toString() ?: "com.cvix"
version = rootProject.findProperty("version")?.toString() ?: "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

idea {
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
    module.excludeDirs.add(file("**/node_modules"))
}

eclipse {
    classpath {
        file {
            whenMerged {
                val cp = this as org.gradle.plugins.ide.eclipse.model.Classpath
                cp.entries.add(
                    org.gradle.plugins.ide.eclipse.model.SourceFolder(
                        "build/generated/sources/annotationProcessor/java/main",
                        null,
                    ),
                )
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeTags("unit")
    }
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

defaultTasks("bootRun")
