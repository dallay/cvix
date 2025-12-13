package com.cvix.resume.application.generate

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.resume.domain.DocumentType
import com.cvix.resume.domain.Locale
import com.cvix.resume.domain.PdfGenerator
import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateRenderer
import com.cvix.resume.domain.event.GeneratedDocumentEvent
import com.cvix.resume.domain.exception.TemplateAccessDeniedException
import com.cvix.resume.domain.exception.TemplateNotFoundException
import com.cvix.subscription.domain.SubscriptionTier
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * High-performance, non-blocking PDF resume generator.
 *
 * Optimizations:
 * - Template rendering runs on IO dispatcher to avoid blocking reactive threads
 * - Streaming PDF generation with backpressure support
 * - Zero-copy stream wrapping for monitoring
 * - Atomic operations for thread-safe metrics tracking
 * - Efficient timing using measureTimeMillis
 * - Configurable timeouts for all operations
 *
 * Security:
 * - Validates user subscription tier before allowing access to premium templates
 * - Uses TemplateRepository to resolve template metadata and validate permissions
 * - Defaults to FREE tier if user has no active subscription
 */
@Service
class PdfResumeGenerator(
    private val templateRenderer: TemplateRenderer,
    private val pdfGenerator: PdfGenerator,
    private val templateRepository: com.cvix.resume.domain.TemplateRepository,
    private val subscriptionRepository: com.cvix.subscription.domain.SubscriptionRepository,
    eventPublisher: EventPublisher<GeneratedDocumentEvent>
) {
    private val eventBroadcaster = EventBroadcaster<GeneratedDocumentEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Generates a PDF resume from the given resume data and locale.
     *
     * All operations are non-blocking:
     * - Template resolution and permission validation
     * - Template rendering: offloaded to IO dispatcher
     * - PDF generation: reactive stream with backpressure
     * - Event publishing: asynchronous
     *
     * @param templateId The ID of the resume template to use
     * @param resume The resume data following JSON Resume schema
     * @param userId The ID of the user requesting the resume (for permission validation)
     * @param locale The locale for the resume (default is EN)
     * @return An InputStream of the generated PDF
     * @throws com.cvix.resume.domain.exception.TemplateAccessDeniedException if user lacks required subscription tier
     * @throws com.cvix.resume.domain.exception.TemplateNotFoundException if template not found
     * @throws kotlinx.coroutines.TimeoutCancellationException if generation exceeds timeout
     * @throws Exception if there is an error during generation
     */
    suspend fun generate(
        templateId: String,
        resume: Resume,
        userId: UUID,
        locale: Locale = Locale.EN
    ): InputStream {
        val requestId = UUID.randomUUID()
        val startTimeNanos = System.nanoTime()
        log.info(
            "Resume generation started - requestId={}, templateId={}, userId={}, locale={}," +
                "hasWork={}, hasEducation={}, hasSkills={}",
            requestId,
            templateId,
            userId,
            locale.code,
            resume.work.isNotEmpty(),
            resume.education.isNotEmpty(),
            resume.skills.isNotEmpty(),
        )

        return try {
            // Apply global timeout for entire operation
            withTimeout(GENERATION_TIMEOUT_MS) {
                // Step 1: Resolve template metadata and validate permissions
                val templateMetadata = templateRepository.findById(templateId)
                    ?: throw TemplateNotFoundException(templateId)

                // Step 2: Get user's subscription tier (default to FREE if no active subscription)
                val userSubscription = subscriptionRepository.findActiveByUserId(userId)
                val userTier =
                    userSubscription?.tier ?: SubscriptionTier.FREE

                log.debug(
                    "Validating template access - requestId={}, templateId={}, requiredTier={}, userTier={}",
                    requestId,
                    templateId,
                    templateMetadata.requiredSubscriptionTier,
                    userTier,
                )

                // Step 3: Validate user has required subscription tier
                validateUserHasRequiredSubscriptionTier(
                    templateMetadata,
                    userTier,
                    requestId,
                    templateId,
                )

                log.debug(
                    "Template access granted - requestId={}, templateId={}, templatePath={}",
                    requestId,
                    templateId,
                    templateMetadata.templatePath,
                )

                // Step 4: Render LaTeX template (offload to IO dispatcher)
                val latexSource = renderLatexTemplate(requestId, templateMetadata, resume, locale)

                // Step 5: Generate PDF reactively (non-blocking)
                log.debug("Generating PDF - requestId={}", requestId)

                val pdfStream = pdfGenerator.generatePdf(latexSource, locale.code)
                    .awaitSingle()

                // Wrapper for metrics tracking (zero-copy, non-blocking)
                MonitoredInputStream(pdfStream, requestId, startTimeNanos, eventBroadcaster, locale)
            }
        } catch (error: Throwable) {
            val durationMs = (System.nanoTime() - startTimeNanos) / NANOS_TO_MILLIS

            log.error(
                "Resume generation failed - requestId={}, duration={}ms, error={}",
                requestId,
                durationMs,
                error.message,
                error,
            )
            throw error
        }
    }

    private suspend fun renderLatexTemplate(
        requestId: UUID?,
        templateMetadata: TemplateMetadata,
        resume: Resume,
        locale: Locale
    ): String {
        return withContext(Dispatchers.IO) {
            log.debug("Rendering template - requestId={}", requestId)
            lateinit var latexResult: String
            val durationMs = measureTimeMillis {
                latexResult = templateRenderer.render(
                    templateMetadata.templatePath,
                    resume,
                    locale.code,
                )
            }

            log.debug(
                "Template rendered - requestId={}, duration={}ms, size={}",
                requestId,
                durationMs,
                latexResult.length,
            )
            latexResult
        }
    }

    private fun validateUserHasRequiredSubscriptionTier(
        templateMetadata: TemplateMetadata,
        userTier: SubscriptionTier,
        requestId: UUID?,
        templateId: String
    ) {
        if (!templateMetadata.isAccessibleBy(userTier)) {
            log.warn(
                "Template access denied - requestId={}, templateId={}, requiredTier={}, userTier={}",
                requestId,
                templateId,
                templateMetadata.requiredSubscriptionTier,
                userTier,
            )
            throw TemplateAccessDeniedException(
                templateId = templateId,
                requiredTier = templateMetadata.requiredSubscriptionTier,
                userTier = userTier,
            )
        }
    }

    /**
     * Zero-copy InputStream wrapper that tracks metrics without buffering.
     *
     * Thread-safe implementation using atomic operations.
     * Events are published asynchronously on close.
     */
    private class MonitoredInputStream(
        private val delegate: InputStream,
        private val requestId: UUID,
        private val startTimeNanos: Long,
        private val eventBroadcaster: EventBroadcaster<GeneratedDocumentEvent>,
        private val locale: Locale
    ) : InputStream() {
        private val bytesRead = AtomicLong(0)
        private val eventPublished = AtomicBoolean(false)

        override fun read(): Int {
            val byte = delegate.read()
            if (byte != -1) {
                bytesRead.incrementAndGet()
            }
            return byte
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val count = delegate.read(b, off, len)
            if (count > 0) {
                bytesRead.addAndGet(count.toLong())
            }
            return count
        }

        override fun available(): Int = delegate.available()

        override fun skip(n: Long): Long {
            val skipped = delegate.skip(n)
            if (skipped > 0) {
                bytesRead.addAndGet(skipped)
            }
            return skipped
        }

        override fun markSupported(): Boolean = delegate.markSupported()

        override fun mark(readlimit: Int) = delegate.mark(readlimit)

        override fun reset() = delegate.reset()

        override fun close() {
            try {
                delegate.close()
            } finally {
                // Publish metrics once, even if close is called multiple times
                if (eventPublished.compareAndSet(false, true)) {
                    val durationMs = (System.nanoTime() - startTimeNanos) / NANOS_TO_MILLIS
                    val totalBytes = bytesRead.get()

                    log.info(
                        "Resume generation completed - requestId={}, duration={}ms, size={} bytes",
                        requestId,
                        durationMs,
                        totalBytes,
                    )

                    // Publish event asynchronously (non-blocking)
                    // GlobalScope is appropriate here: event publishing must outlive the InputStream lifecycle
                    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                    @Suppress("GlobalCoroutineUsage") // Event publishing must outlive the stream lifecycle
                    GlobalScope.launch {
                        try {
                            eventBroadcaster.publish(
                                GeneratedDocumentEvent(
                                    id = requestId,
                                    documentType = DocumentType.RESUME,
                                    locale = locale,
                                    sizeInBytes = totalBytes,
                                ),
                            )
                        } catch (e: Exception) {
                            log.warn(
                                "Failed to publish GeneratedDocumentEvent - requestId={}",
                                requestId,
                                e,
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(PdfResumeGenerator::class.java)

        // Timeout for entire PDF generation pipeline (template render + PDF creation)
        // Increased to 120 seconds to accommodate CI environments and Docker image pulls
        private const val GENERATION_TIMEOUT_MS = 120_000L // 120 seconds

        // Conversion constants
        private const val NANOS_TO_MILLIS = 1_000_000L
    }
}
