package com.cvix.resume.application.template

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.query.QueryHandler
import com.cvix.resume.application.TemplateMetadataResponses
import com.cvix.subscription.domain.ResolverContext
import com.cvix.subscription.domain.SubscriptionResolver
import com.cvix.workspace.application.security.WorkspaceAuthorizationService
import com.cvix.workspace.domain.WorkspaceAuthorizationException
import org.slf4j.LoggerFactory

@Service
class ListTemplatesQueryHandler(
    private val templateCatalog: TemplateCatalog,
    private val workspaceAuthorizationService: WorkspaceAuthorizationService,
    private val subscriptionResolver: SubscriptionResolver,
) : QueryHandler<ListTemplatesQuery, TemplateMetadataResponses> {

    /**
     * Handles the ListTemplatesQuery to retrieve available resume templates.
     *
     * The handler resolves the user's subscription tier and filters templates accordingly,
     * ensuring users only see templates they have access to based on their subscription plan.
     *
     * @param query The ListTemplatesQuery containing user and workspace context
     * @return A TemplateMetadataResponses containing the list of templates accessible to the user
     * @throws [WorkspaceAuthorizationException] If the user does not have access to the workspace.
     */
    override suspend fun handle(query: ListTemplatesQuery): TemplateMetadataResponses {
        log.debug("Listing templates for user={} limit={}", query.userId, query.limit)

        // Authorization check: ensure user is a member of the workspace
        workspaceAuthorizationService.ensureAccess(query.workspaceId, query.userId)

        // Resolve user's subscription tier (defaults to FREE if not found)
        val context = ResolverContext.UserId(query.userId)
        val subscriptionTier = subscriptionResolver.resolve(context)
        log.debug("Resolved subscription tier {} for user {}", subscriptionTier, query.userId)

        val listTemplates = templateCatalog.listTemplates(
            subscriptionTier = subscriptionTier,
            limit = query.limit,
        )
        return TemplateMetadataResponses.from(listTemplates)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ListTemplatesQueryHandler::class.java)
    }
}
