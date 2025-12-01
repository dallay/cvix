package com.cvix.resume.application.generate

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.resume.domain.DocumentType
import com.cvix.resume.domain.Locale
import com.cvix.resume.domain.PdfGenerator
import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.TemplateRenderer
import com.cvix.resume.domain.event.GeneratedDocumentEvent
import java.io.InputStream
import java.util.*
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
 */
@Service
class PdfResumeGenerator(
    private val templateRenderer: TemplateRenderer,
    private val pdfGenerator: PdfGenerator,
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
     * - Template rendering: offloaded to IO dispatcher
     * - PDF generation: reactive stream with backpressure
     * - Event publishing: asynchronous
     *
     * @param resume The resume data following JSON Resume schema
     * @param locale The locale for the resume (default is EN)
     * @return An InputStream of the generated PDF
     * @throws kotlinx.coroutines.TimeoutCancellationException if generation exceeds timeout
     * @throws Exception if there is an error during generation
     */
    suspend fun generate(resume: Resume, locale: Locale = Locale.EN): InputStream {
        val requestId = UUID.randomUUID()
        val startTimeNanos = System.nanoTime()

        if (log.isInfoEnabled) {
            log.info(
                "Resume generation started - requestId={}, locale={}, hasWork={}, hasEducation={}, hasSkills={}",
                requestId,
                locale.code,
                resume.work.isNotEmpty(),
                resume.education.isNotEmpty(),
                resume.skills.isNotEmpty(),
            )
        }

        return try {
            // Apply global timeout for entire operation
            withTimeout(GENERATION_TIMEOUT_MS) {
                // Step 1: Render LaTeX template (offload to IO dispatcher)
                val latexSource = withContext(Dispatchers.IO) {
                    if (log.isDebugEnabled) {
                        log.debug("Rendering template - requestId={}", requestId)
                    }

                    var latex: String
                    val renderTime = measureTimeMillis {
                        latex = templateRenderer.render(resume, locale.code)
                    }

                    if (log.isDebugEnabled) {
                        log.debug(
                            "Template rendered - requestId={}, duration={}ms, size={}",
                            requestId,
                            renderTime,
                            latex.length,
                        )
                    }
                    latex
                }

                // Step 2: Generate PDF reactively (non-blocking)
                if (log.isDebugEnabled) {
                    log.debug("Generating PDF - requestId={}", requestId)
                }

                val pdfStream = pdfGenerator.generatePdf(latexSource, locale.code)
                    .awaitSingle()

                // Wrapper for metrics tracking (zero-copy, non-blocking)
                MonitoredInputStream(pdfStream, requestId, startTimeNanos, eventBroadcaster, locale)
            }
        } catch (error: Throwable) {
            val durationMs = (System.nanoTime() - startTimeNanos) / NANOS_TO_MILLIS

            if (log.isErrorEnabled) {
                log.error(
                    "Resume generation failed - requestId={}, duration={}ms, error={}",
                    requestId,
                    durationMs,
                    error.message,
                    error,
                )
            }
            throw error
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

                    if (log.isInfoEnabled) {
                        log.info(
                            "Resume generation completed - requestId={}, duration={}ms, size={} bytes",
                            requestId,
                            durationMs,
                            totalBytes,
                        )
                    }

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
