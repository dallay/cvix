package com.cvix

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class ArchTest {

    private lateinit var importedClasses: JavaClasses
    private val boundedContexts = listOf(
        "users",
        "authentication",
        "workspace",
        "ratelimit",
        "resume",
    )

    @BeforeEach
    fun setUp() {
        importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.cvix")
    }

    @Test
    fun domainShouldNotDependOnApplicationOrInfrastructure() {

        boundedContexts.forEach { context ->
            ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage("com.cvix.$context.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                    "com.cvix.$context.application..",
                    "com.cvix.$context.infrastructure..",
                )
                .because("The 'domain' package in '$context' should not depend on 'application' or 'infrastructure'")
                .check(importedClasses)
        }
    }

    @Test
    fun applicationShouldNotDependOnInfrastructure() {

        boundedContexts.forEach { context ->
            ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage("com.cvix.$context.application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.cvix.$context.infrastructure..")
                .because("The 'application' package in '$context' should not depend on 'infrastructure'")
                .check(importedClasses)
        }
    }
}
