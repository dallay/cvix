package com.cvix.resume.application.generate

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandWithResultHandler
import com.cvix.resume.domain.Locale
import com.cvix.subscription.domain.ResolverContext
import com.cvix.subscription.domain.SubscriptionResolver
import java.io.InputStream
import org.slf4j.LoggerFactory

/**
 * Command handler for generating PDF resumes.
 * Orchestrates the template rendering and PDF generation process.
 */
@Service
class GenerateResumeCommandHandler(
    private val pdfGenerator: PdfResumeGenerator,
    private val subscriptionResolver: SubscriptionResolver
) : CommandWithResultHandler<GenerateResumeCommand, InputStream> {

    /**
     * Handles the resume generation command.
     * @param command The command containing resume data, userId, and locale
     * @return The PDF as an InputStream
     */
    override suspend fun handle(command: GenerateResumeCommand): InputStream {
        val locale = try {
            Locale.from(command.locale.code)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Unsupported locale: ${command.locale}", e)
        }
        log.debug(
            "Handling GenerateResumeCommand - templateId={}, userId={}, locale={}",
            command.templateId, command.userId, locale.code,
        )

        // Resolve user's subscription tier (defaults to FREE if not found)
        val context = ResolverContext.UserId(command.userId)
        val subscriptionTier = subscriptionResolver.resolve(context)
        log.debug("Resolved subscription tier {} for user {}", subscriptionTier, command.userId)

        return pdfGenerator.generate(command.templateId, command.resume, command.userId, subscriptionTier, locale)
    }

    companion object {
        private val log = LoggerFactory.getLogger(GenerateResumeCommandHandler::class.java)
    }
}
