package com.cvix.resume.infrastructure.template.source

import com.cvix.resume.domain.TemplateRepository
import com.cvix.resume.domain.TemplateSourceKeys
import com.cvix.resume.domain.TemplateSourceStrategy
import com.cvix.resume.domain.TemplateSourceType
import com.cvix.subscription.domain.SubscriptionTier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Factory implementation of [TemplateSourceStrategy] that selects template repositories
 * based on application configuration.
 *
 * This factory manages a registry of all available [TemplateRepository] implementations
 * and provides access to them based on the configured source types in
 * [TemplateSourceProperties].
 *
 * Configuration example:
 * ```yaml
 * resume:
 *   template:
 *     source:
 *       type: CLASSPATH  # or FILESYSTEM
 *       path: templates/resume
 * ```
 *
 * @param templateSources Map of template source type names to their repository implementations
 * @param properties Configuration properties defining which source(s) to use
 * @created 11/12/25
 */
@Component
class TemplateSourceRepositoryFactory(
    private val templateSources: Map<String, TemplateRepository>,
    private val properties: TemplateSourceProperties
) : TemplateSourceStrategy {

    /**
     * Retrieves the [TemplateRepository] for the specified source type.
     * @param sourceType The template source type
     * @return The corresponding TemplateRepository
     * @throws [IllegalArgumentException] if the source type is unsupported
     */
    fun get(sourceType: String): TemplateRepository =
        templateSources[sourceType]
            ?: throw IllegalArgumentException("Unsupported source: $sourceType. Available: ${templateSources.keys}")

    /**
     * Returns all available template source types registered in this factory.
     * @return Set of registered source type names
     */
    fun availableSources(): Set<String> = templateSources.keys

    /**
     * Returns the list of active [TemplateRepository] instances based on configuration and subscription tier.
     *
     * Supports multiple repositories for multi-source scenarios (e.g., premium users
     * accessing both filesystem and classpath templates).
     *
     * The order of repositories matters: First repository has highest priority for ID conflicts.
     * This allows premium templates to override default ones when they share the same ID.
     *
     * Configuration examples:
     * - Free: types: [CLASSPATH, OTHER]
     * - Premium: types: [FILESYSTEM, CLASSPATH]
     *
     * @param subscriptionTier The user's subscription tier for context-aware repository selection
     * @return List of active [TemplateRepository] instances in priority order
     * @throws IllegalArgumentException if any configured source type is not available, or if none resolve
     *
     * @implNote This method fails fast: if any configured source type is missing from the available sources,
     *           it throws an exception listing all missing types and their expected bean names. It also throws
     *           if no repositories are resolved at all.
     */
    override suspend fun activeTemplateRepositories(subscriptionTier: SubscriptionTier): List<TemplateRepository> {

        val configuredTypes = properties.source.types.ifEmpty {
            listOf(TemplateSourceType.CLASSPATH)
        }

        log.debug(
            "Resolving active template repositories for tier: {} and types: {}",
            subscriptionTier,
            configuredTypes,
        )

        // Check for missing repositories before mapping
        val missingTypes = configuredTypes.mapNotNull { sourceType ->
            val repositoryBeanName = when (sourceType) {
                TemplateSourceType.CLASSPATH -> TemplateSourceKeys.CLASSPATH
                TemplateSourceType.FILESYSTEM -> TemplateSourceKeys.FILESYSTEM
            }
            if (templateSources[repositoryBeanName] == null) {
                sourceType to repositoryBeanName
            } else {
                null
            }
        }
        if (missingTypes.isNotEmpty()) {
            val missingMsg = missingTypes.joinToString(", ") { (type, bean) ->
                "type '$type' (expected bean: '$bean')"
            }
            throw IllegalArgumentException(
                "Missing template repository for configured source type(s): $missingMsg. " +
                    "Available sources: ${templateSources.keys}"
            )
        }

        // All required repositories are present; map to actual repositories
        val repositories = configuredTypes.map { sourceType ->
            val repositoryBeanName = when (sourceType) {
                TemplateSourceType.CLASSPATH -> TemplateSourceKeys.CLASSPATH
                TemplateSourceType.FILESYSTEM -> TemplateSourceKeys.FILESYSTEM
            }
            val repository = templateSources[repositoryBeanName]!!
            log.info(
                "Activated template repository: {} (type: {}) for tier: {}",
                repositoryBeanName,
                sourceType,
                subscriptionTier,
            )
            repository
        }

        if (repositories.isEmpty()) {
            throw IllegalArgumentException(
                "No valid template repositories found for configured types: $configuredTypes. " +
                    "Available sources: ${templateSources.keys}",
            )
        }

        log.info("Total active repositories: {} for tier: {}", repositories.size, subscriptionTier)
        return repositories
    }


    companion object {
        private val log = LoggerFactory.getLogger(TemplateSourceRepositoryFactory::class.java)
    }
}
