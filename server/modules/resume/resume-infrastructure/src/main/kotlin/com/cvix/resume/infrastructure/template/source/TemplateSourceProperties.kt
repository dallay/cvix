package com.cvix.resume.infrastructure.template.source

import com.cvix.resume.domain.TemplateSourceType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Configuration properties for resume template loading.
 *
 * This class is bound to properties under the `resume.template` prefix by Spring Boot.
 * It provides a typed, validated representation of how template sources should be resolved at runtime.
 *
 * Behaviour & semantics:
 * - The `source.types` list defines the ordered priority of template sources. The first entry has the
 *   highest priority and will be consulted first when resolving templates or handling ID conflicts
 *   (e.g., premium templates may override classpath templates by appearing earlier in the list).
 * - The `source.path` is used when the FILESYSTEM source type is enabled; it defines where on the
 *   filesystem templates should be loaded from. For CLASSPATH sources this value is typically ignored.
 *
 * Example application.yml usage:
 *
 * resume:
 *   template:
 *     source:
 *       types:
 *         - FILESYSTEM
 *         - CLASSPATH
 *       path: /opt/app/templates/resume
 *
 * Notes:
 * - The class is annotated with `@Validated` so any Bean Validation annotations added later on fields
 *   will be enforced when the properties are bound.
 * - Defaults are provided for both `types` (classpath-only) and `path` (templates/resume) to support a
 *   sensible out-of-the-box configuration for local development and simple deployments.
 *
 * @property source Container with the concrete template source configuration (types + path).
 */
@Validated
@ConfigurationProperties(prefix = "resume.template")
data class TemplateSourceProperties(
    val source: SourceConfig = SourceConfig()
) {
    /**
     * Holds the configuration for template source selection and a filesystem path.
     *
     * Details:
     * - types: Ordered list of [TemplateSourceType] values describing where templates are loaded from.
     *   The list order represents priority: earlier entries override later ones for conflicting template IDs.
     *   Default: [TemplateSourceType.CLASSPATH]
     *
     * - path: Filesystem path (or relative classpath prefix) to search when the FILESYSTEM source is active.
     *   Default: "templates/resume"
     *
     * Examples:
     * - Free tier: types = [CLASSPATH, ANY_OTHER_SOURCE]
     * - Premium tier: types = [FILESYSTEM, CLASSPATH] (filesystem templates take precedence)
     *
     * @property types List of template source types in priority order (first = highest priority).
     * @property path Path to templates used by FILESYSTEM source type.
     */
    data class SourceConfig(
        val types: List<TemplateSourceType> = listOf(TemplateSourceType.CLASSPATH),
        val path: String = "templates/resume"
    )
}
