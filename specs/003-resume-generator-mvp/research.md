# Research: Resume Generator MVP

*Generated: 2025-11-01*
*Feature Spec: `specs/003-resume-generator-mvp/spec.md`*

## Overview

This document captures research findings for technical unknowns identified during implementation planning of the Resume Generator MVP feature.

## Research Tasks

### 1. LaTeX Template Architecture for Adaptive Layouts

**Context**: Spec requires "smart content adaptation" (FR-008) where template adjusts section emphasis based on content (skills-heavy vs experience-heavy resumes).

**Questions**:

- How to implement conditional section sizing in LaTeX templates?
- What LaTeX packages provide responsive layout capabilities?
- How to detect content density and adjust spacing/prominence dynamically?

**Findings**:

**LaTeX Conditional Layout Packages**:

- **`ifthen`**: Basic conditional logic in LaTeX (`\ifthenelse{\lengthtest{\value{skills} > 5}}{...}{...}`)
- **`etoolbox`**: Advanced conditionals and list processing (`\ifnumgreater`, `\ifdef`)
- **`geometry`**: Dynamic page layout adjustments (`\newgeometry{margin=1in}`)
- **`titlesec`**: Custom section formatting with conditional spacing

**Adaptive Layout Strategy**:

```latex
% Calculate content scores (passed as template variables)
\newcommand{\skillsweight}{<skills_count>}
\newcommand{\experienceweight}{<experience_years>}

% Conditional section ordering
\ifthenelse{\skillsweight > \experienceweight}{
  \section{Technical Skills}  % Prominent (larger font, top placement)
  \section{Work Experience}   % Standard
}{
  \section{Work Experience}   % Prominent
  \section{Technical Skills}  % Standard
}

% Dynamic spacing based on content density
\setlength{\parskip}{\ifthenelse{\lengthtest{\totalpages > 1}}{6pt}{8pt}}
```

**Template Variables Required**:

- `skills_count`: Number of skills listed
- `experience_years`: Total years of experience
- `experience_entries`: Number of work history entries
- `education_entries`: Number of degrees/certifications

**Recommendation**: Use StringTemplate 4 over FreeMarker for LaTeX generation:

- **StringTemplate 4**: Designed for code generation, strict separation of logic/presentation, LaTeX-safe by default
- **FreeMarker**: More feature-rich but allows logic leakage, requires manual LaTeX escaping

**Implementation Path**:

1. Create base template with conditional blocks
2. Calculate content metrics in application layer (Kotlin)
3. Pass metrics as template variables
4. Let LaTeX conditionals handle layout adaptation

**References**:

- StringTemplate 4 docs: <https://github.com/antlr/stringtemplate4>
- LaTeX conditional packages: CTAN `ifthen`, `etoolbox`

---

### 2. JSON Resume Schema Implementation

**Context**: Spec requires support for JSON Resume standard (<https://jsonresume.org/schema>) for data portability.

**Questions**:

- What are the mandatory vs optional fields?
- How to map JSON Resume schema to Kotlin domain entities?
- How to validate the schema on the backend?

**Findings**:

**JSON Resume Schema Structure** (v1.0.0):

```json
{
  "basics": {
    "name": "string",
    "label": "string",         // Job title
    "image": "url",            // Profile photo (optional)
    "email": "string",
    "phone": "string",
    "url": "url",              // Personal website
    "summary": "string",
    "location": {
      "city": "string",
      "countryCode": "string"
    },
    "profiles": [              // Social media
      {"network": "string", "url": "url"}
    ]
  },
  "work": [
    {
      "name": "string",        // Company name
      "position": "string",
      "startDate": "YYYY-MM-DD",
      "endDate": "YYYY-MM-DD", // or null for "Present"
      "summary": "string",
      "highlights": ["string"]
    }
  ],
  "education": [
    {
      "institution": "string",
      "area": "string",        // Field of study
      "studyType": "string",   // Degree type
      "startDate": "YYYY-MM-DD",
      "endDate": "YYYY-MM-DD"
    }
  ],
  "skills": [
    {
      "name": "string",        // Skill category
      "keywords": ["string"]   // Individual skills
    }
  ],
  "languages": [
    {"language": "string", "fluency": "string"}
  ],
  "projects": [
    {
      "name": "string",
      "description": "string",
      "url": "url"
    }
  ]
}
```

**Mandatory Fields** (minimum viable resume):

- `basics.name`
- `basics.email`
- At least ONE of: `work[]`, `education[]`, `skills[]`

**Kotlin Domain Mapping**:

```kotlin
// Domain entities (pure Kotlin, no framework deps)
data class ResumeData(
    val basics: PersonalInfo,
    val work: List<WorkExperience> = emptyList(),
    val education: List<Education> = emptyList(),
    val skills: List<SkillCategory> = emptyList(),
    val languages: List<Language> = emptyList(),
    val projects: List<Project> = emptyList()
)

data class PersonalInfo(
    val name: String,          // Max 100 chars (FR-004)
    val label: String?,        // Job title, max 100 chars
    val email: String,
    val phone: String?,
    val url: String?,
    val summary: String?,      // Max 500 chars
    val location: Location?,
    val profiles: List<SocialProfile> = emptyList()
)

data class WorkExperience(
    val company: String,       // Max 100 chars
    val position: String,      // Max 100 chars
    val startDate: LocalDate,
    val endDate: LocalDate?,   // null = "Present"
    val summary: String?,      // Max 500 chars
    val highlights: List<String> = emptyList()
)

// ... similar for Education, SkillCategory, etc.
```

**Validation Strategy**:

1. **JSON Schema validation** (OpenAPI 3.0 spec with `@Schema` annotations)
2. **Zod schema** (frontend form validation)
3. **Backend validation** (Spring Validation + custom validators):
   - `@NotBlank` for mandatory fields
   - `@Size(max=100)` for names/titles
   - `@Size(max=500)` for descriptions
   - `@Email` for email format
   - Custom validator for date range logic (startDate < endDate)

**JSON Resume Compliance**:

- ✅ Accept standard JSON Resume input
- ✅ Validate required fields per spec
- ❌ **Do NOT support** `image` field in MVP (profile photos add complexity)
- ❌ **Do NOT support** `awards`, `publications`, `references` (out of scope for MVP)

**Recommendation**: Use subset of JSON Resume schema for MVP, document limitations in API.

**References**:

- JSON Resume Schema: <https://jsonresume.org/schema>
- OpenAPI 3.0 validation: <https://spec.openapis.org/oas/v3.0.3>

---

### 3. Docker Container Orchestration for PDF Generation

**Context**: Need strategy for managing ephemeral Docker containers that run `pdflatex` on user input with resource limits (512MB RAM, 0.5 CPU, 10s timeout).

**Questions**:

- What's the best way to spawn/kill Docker containers from Spring Boot?
- How do we enforce resource limits programmatically?
- For concurrent requests (50 users), what's the strategy?
- How are failed containers cleaned up?

**Findings**:

**Docker Java Client Options**:

1. **Docker Java Library** (<https://github.com/docker-java/docker-java>)
   - Mature, official Docker API wrapper
   - Spring Boot integration available
   - Supports: container creation, resource limits, network isolation, volume mounts

2. **Testcontainers** (already in project)
   - Primarily for testing but can be used for runtime containers
   - Simpler API than raw Docker Java
   - Built-in cleanup mechanisms

**Recommendation**: Use **Docker Java Library** for production (Testcontainers is test-focused).

**Container Lifecycle Pattern**:

```kotlin
// Infrastructure layer adapter
class DockerPdfGeneratorAdapter(
    private val dockerClient: DockerClient
) : PdfGeneratorPort {

    suspend fun generatePdf(latexContent: String): ByteArray {
        // 1. Create temporary container with resource limits
        val containerId = dockerClient.createContainerCmd("texlive/texlive:TL2024-historic")
            .withName("pdf-gen-${UUID.randomUUID()}")
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withMemory(512L * 1024 * 1024)  // 512MB
                    .withCpuQuota(50000L)            // 0.5 CPU
                    .withReadonlyRootfs(true)        // Security
                    .withNetworkMode("none")         // No network
            )
            .withCmd("pdflatex", "-interaction=nonstopmode", "resume.tex")
            .exec()

        try {
            // 2. Copy LaTeX content into container
            dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(createTarWithContent(latexContent))
                .exec()

            // 3. Start container with timeout
            dockerClient.startContainerCmd(containerId).exec()

            val exitCode = withTimeout(10.seconds) {
                waitForContainerExit(containerId)
            }

            if (exitCode != 0) {
                throw PdfGenerationException("LaTeX compilation failed")
            }

            // 4. Extract generated PDF
            return dockerClient.copyArchiveFromContainerCmd(containerId, "/resume.pdf")
                .exec()
                .readBytes()

        } finally {
            // 5. Always clean up (even on failure)
            dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec()
        }
    }
}
```

**Concurrency Strategy** (50 concurrent users):

- **Semaphore-based throttling**: Limit concurrent Docker containers to avoid resource exhaustion

```kotlin
private val containerSemaphore = Semaphore(10) // Max 10 concurrent containers

suspend fun generatePdf(latexContent: String): ByteArray {
    containerSemaphore.withPermit {
        // Container creation logic here
    }
}
```

- **Alternative**: Use Kotlin Flow with `flowOn(Dispatchers.IO.limitedParallelism(10))`

**Error Handling**:

- **Timeout (>10s)**: Kill container, return HTTP 504 Gateway Timeout
- **OOM (>512MB)**: Container auto-killed by Docker, return HTTP 500
- **LaTeX compilation error**: Parse stderr, return HTTP 422 with error details
- **Orphaned containers**: Background cleanup job (`@Scheduled`) removes containers older than 1 hour

**Resource Monitoring**:

```kotlin
@Scheduled(fixedRate = 60000) // Every minute
fun cleanupOrphanedContainers() {
    val containers = dockerClient.listContainersCmd()
        .withLabelFilter(mapOf("app" to "resume-generator"))
        .withStatusFilter(listOf("exited", "dead"))
        .exec()

    containers.forEach { container ->
        dockerClient.removeContainerCmd(container.id).withForce(true).exec()
        logger.warn("Cleaned up orphaned container: ${container.id}")
    }
}
```

**Security Considerations**:

- ✅ `--read-only` filesystem (prevent malicious package installation)
- ✅ `--no-new-privileges` (prevent privilege escalation)
- ✅ `--network=none` (no internet access from container)
- ✅ Resource limits enforced by Docker daemon
- ✅ Container lifetime <10s (minimize attack surface)

**Alternative Considered: Kubernetes Jobs**:

- **Pros**: Better at scale, resource isolation, built-in retries
- **Cons**: Over-engineered for MVP (50 users), requires K8s cluster, operational complexity
- **Decision**: Defer to post-MVP if scale requires

**References**:

- Docker Java: <https://github.com/docker-java/docker-java>
- Docker security: <https://docs.docker.com/engine/security/>

---

### 4. LaTeX Internationalization (i18n) for English/Spanish

**Context**: Spec requires two languages (English/Spanish) with localized date formats (FR-005) and error messages.

**Questions**:

- How to handle LaTeX i18n (fonts, hyphenation, date formatting)?
- What LaTeX packages support Spanish properly?
- How to integrate with Vue I18n (frontend) and Spring i18n (backend)?

**Findings**:

**LaTeX i18n Packages**:

- **`babel`**: Standard i18n package, supports 200+ languages including Spanish
- **`datetime2`**: Localized date formatting
- **`fontenc`**: Proper character encoding for accented characters (á, é, í, ó, ú, ñ)

**LaTeX Template i18n Setup**:

```latex
\usepackage[utf8]{inputenc}     % UTF-8 input encoding
\usepackage[T1]{fontenc}        % 8-bit font encoding (accents)
\usepackage[english,spanish]{babel}  % Load both languages

% Set language (passed as template variable: <language>)
\selectlanguage{<language>}

% Localized date formatting
\usepackage[useregional]{datetime2}
\DTMsetdatestyle{iso}  % Will use locale-specific format
```

**Date Format Localization**:

- **English**: "Jan 2020 - Present"
- **Spanish**: "Ene 2020 - Presente"

**Strategy**:

1. **Backend**: Format dates using `java.time.format.DateTimeFormatter` with `Locale`:

   ```kotlin
   fun formatWorkPeriod(start: LocalDate, end: LocalDate?, locale: Locale): String {
       val formatter = DateTimeFormatter.ofPattern("MMM yyyy", locale)
       val startStr = start.format(formatter)
       val endStr = end?.format(formatter) ?: when (locale.language) {
           "es" -> "Presente"
           else -> "Present"
       }
       return "$startStr - $endStr"
   }
   ```

2. **LaTeX**: Receive pre-formatted strings from backend (no LaTeX date logic needed)

**Section Headings Localization**:

Create language-specific template variables:

```latex
% English template
\section{<section_work>}         % "Work Experience"
\section{<section_education>}    % "Education"
\section{<section_skills>}       % "Technical Skills"

% Spanish template (separate .tex file OR conditional blocks)
\section{<section_work>}         % "Experiencia Laboral"
\section{<section_education>}    % "Educación"
\section{<section_skills>}       % "Habilidades Técnicas"
```

**Implementation Options**:

**Option A: Separate Templates** (Recommended for MVP):

- `resume-template-en.tex`
- `resume-template-es.tex`
- Simpler logic, easier to maintain translations

**Option B: Single Template with Conditionals**:

```latex
\ifthenelse{\equal{<language>}{spanish}}{
  \newcommand{\sectionwork}{Experiencia Laboral}
}{
  \newcommand{\sectionwork}{Work Experience}
}
```

**Frontend-Backend i18n Integration**:

1. **Frontend (Vue I18n)**: Form labels, error messages in user's preferred language
2. **Backend**: Accepts `Accept-Language` header, stores locale with resume generation request
3. **LaTeX Template**: Receives locale-specific strings (pre-translated by backend)

**Font Considerations**:

- TeX Live includes Latin Modern fonts (support Spanish characters)
- No custom font setup needed for MVP
- If custom fonts required: `fontspec` package with XeLaTeX (heavier than pdfLaTeX)

**Recommendation**: Use **Option A (separate templates)** for MVP, migrate to Option B if supporting 5+ languages in the future.

**References**:

- Babel package: CTAN `babel`
- datetime2 package: CTAN `datetime2`
- Spanish LaTeX guide: <https://www.overleaf.com/learn/latex/International_language_support>

---

### 5. Rate Limiting Implementation for Spring WebFlux

**Context**: Spec requires rate limiting (10 req/min/user) with HTTP 429 response and retry-after timing (FR-013, FR-023).

**Questions**:

- What's the best approach for implementing rate limiting in reactive Spring Boot?
- How do we track per-user request counts effectively?
- What's the strategy for setting the `Retry-After` header?

**Findings**:

**Existing Implementation**:

The project already has **Bucket4j** implemented in the authentication module (`com.loomify.engine.ratelimit`). This is the **recommended approach** to maintain consistency across the codebase.

**Bucket4j Architecture** (already in use):

```text
domain/port/
└── RateLimiterPort.kt              # Domain port (interface)

infrastructure/
├── Bucket4jRateLimiter.kt          # Adapter implementing RateLimiterPort
├── PricingPlan.kt                  # Rate limit tier configuration
├── PricingPlanService.kt           # Per-user bucket management
└── config/
    └── BucketConfigurationStrategy.kt  # Bucket4j configuration builder
```

**Key Features Already Available**:

- ✅ Token bucket algorithm (Bucket4j v8 API)
- ✅ Per-user rate limiting with in-memory cache
- ✅ Pricing plan tiers (FREE, BASIC, PROFESSIONAL)
- ✅ Ready for Redis/Hazelcast backends (distributed rate limiting)
- ✅ Reactive support (works with Spring WebFlux)

**Implementation Pattern for Resume Generator**:

Reuse the existing `RateLimiterPort` and `Bucket4jRateLimiter`:

```kotlin
// Resume-specific rate limit configuration
@Configuration
class ResumeRateLimitConfig {

    @Bean
    fun resumeGenerationRateLimit(): RateLimitProperties {
        return RateLimitProperties(
            capacity = 10,              // 10 requests
            refillTokens = 10,          // Refill 10 tokens
            refillPeriod = Duration.ofMinutes(1)  // Every minute
        )
    }
}

// Controller with rate limiting (leveraging existing infrastructure)
@RestController
@RequestMapping("/api/resume")
class ResumeController(
    private val rateLimiter: RateLimiterPort,  // Injected from existing infra
    private val generateResumeHandler: GenerateResumeCommandHandler
) {

    @PostMapping(produces = ["application/pdf"])
    suspend fun generateResume(
        @RequestBody @Valid request: GenerateResumeRequest,
        @AuthenticationPrincipal user: JwtAuthenticationToken
    ): ResponseEntity<ByteArray> {

        val userId = user.name // JWT subject

        // Check rate limit using existing port
        val rateLimitResult = rateLimiter.checkLimit(userId)

        if (!rateLimitResult.allowed) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", rateLimitResult.retryAfterSeconds.toString())
                .header("X-RateLimit-Limit", "10")
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", rateLimitResult.resetTimestamp.toString())
                .body(null)
        }

        // Generate PDF
        val pdfBytes = generateResumeHandler.handle(
            GenerateResumeCommand(request, userId)
        )

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "attachment; filename=resume.pdf")
            .body(pdfBytes)
    }
}
```

**Per-User Rate Limiting** (already supported):

The existing `PricingPlanService` maintains an in-memory cache of Bucket4j buckets, one per unique user ID:

```kotlin
// From existing PricingPlanService.kt
private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

fun resolveBucket(apiKey: String): Bucket {
    return cache.computeIfAbsent(apiKey) { key ->
        val plan = resolvePricingPlan(key)
        createNewBucket(plan)
    }
}
```

**For Production** (distributed scenario):

The existing implementation is ready for Redis backend. Update configuration:

```yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour

bucket4j:
  enabled: true
  filters:
    - cache-name: buckets
      url: /api/.*
      rate-limits:
        - cache-key: "user-#{principal.name}"
          bandwidths:
            - capacity: 10
              time: 1
              unit: minutes
```

**HTTP 429 Response Format**:

```json
{
  "error": {
    "code": "rate_limit_exceeded",
    "message": "You have exceeded the rate limit of 10 requests per minute. Please try again later.",
    "retryAfter": 60
  }
}
```

**Localized Error Messages**:

Reuse existing Spring i18n infrastructure:

```kotlin
// MessageSource configuration (Spring i18n)
// messages.properties
rate_limit_exceeded=You have exceeded the rate limit of {0} requests per minute. Please try again later.

// messages_es.properties
rate_limit_exceeded=Has excedido el límite de {0} solicitudes por minuto. Por favor, inténtalo de nuevo más tarde.

// Usage in exception handler
@ControllerAdvice
class GlobalExceptionHandler(private val messageSource: MessageSource) {

    @ExceptionHandler(RequestNotPermitted::class)
    fun handleRateLimitExceeded(
        e: RequestNotPermitted,
        locale: Locale
    ): ResponseEntity<ErrorResponse> {
        val message = messageSource.getMessage(
            "rate_limit_exceeded",
            arrayOf(10),
            locale
        )
        return ResponseEntity.status(429)
            .header("Retry-After", "60")
            .body(ErrorResponse("rate_limit_exceeded", message))
    }
}
```

**Testing Rate Limiting**:

```kotlin
@WebFluxTest(ResumeController::class)
class RateLimitingTest {

    @MockkBean
    private lateinit var rateLimiter: RateLimiterPort

    @Test
    fun `should return 429 after 10 requests in 1 minute`() = runBlocking {
        // Mock rate limiter to allow first 10 requests
        repeat(10) { count ->
            coEvery { rateLimiter.checkLimit(any()) } returns RateLimitResult(
                allowed = true,
                remainingTokens = 10 - count - 1
            )

            webTestClient.post()
                .uri("/api/resume")
                .bodyValue(validResumeRequest)
                .exchange()
                .expectStatus().isOk
        }

        // 11th request should be rate limited
        coEvery { rateLimiter.checkLimit(any()) } returns RateLimitResult(
            allowed = false,
            remainingTokens = 0,
            retryAfterSeconds = 60
        )

        webTestClient.post()
            .uri("/api/resume")
            .bodyValue(validResumeRequest)
            .exchange()
            .expectStatus().isEqualTo(429)
            .expectHeader().exists("Retry-After")
            .expectHeader().valueEquals("Retry-After", "60")
    }
}
```

**Integration with Existing Infrastructure**:

1. **Reuse `RateLimiterPort`**: The domain port is already defined and tested
2. **Extend `PricingPlanService`**: Add resume generation limits to existing pricing tiers:

   ```kotlin
   // Add to existing PricingPlan.kt
   enum class PricingPlan {
       FREE(
           apiLimit = 20,
           resumeGenerationLimit = 10  // NEW: 10 resumes per minute
       ),
       BASIC(
           apiLimit = 100,
           resumeGenerationLimit = 50  // NEW: 50 resumes per minute
       ),
       PROFESSIONAL(
           apiLimit = 1000,
           resumeGenerationLimit = 200  // NEW: 200 resumes per minute
       )
   }
   ```

3. **Inject existing adapter**: No new implementation needed

**Recommendation**:

- ✅ **Use existing Bucket4j infrastructure** (consistency across codebase)
- ✅ Add resume-specific rate limits to existing `PricingPlan` tiers
- ✅ Reuse `RateLimiterPort` and `Bucket4jRateLimiter` adapter
- ✅ Production-ready: Already configured for Redis backend

**References**:

- Bucket4j documentation: <https://bucket4j.com/>
- Existing implementation: `server/engine/src/main/kotlin/com/loomify/engine/ratelimit/`
- RFC 6585 (HTTP 429): <https://tools.ietf.org/html/rfc6585#section-4>

---

## Summary

All 5 research tasks completed. Key findings:

1. ✅ LaTeX adaptive layouts: Use `ifthen`/`etoolbox` packages with content metrics from backend
2. ✅ JSON Resume schema: Kotlin domain entities map cleanly to schema, validate with Spring Validation + Zod
3. ✅ Docker orchestration: Use Docker Java Library with semaphore-based throttling (10 concurrent containers)
4. ✅ LaTeX i18n: Separate templates per language (MVP), `babel` package for proper Spanish support
5. ✅ Rate limiting: **Reuse existing Bucket4j infrastructure** from auth module (consistency, production-ready with Redis backend)

**Unresolved Questions** (defer to Phase 1):

- None - all critical technical unknowns resolved

**Next Steps**: Proceed to Phase 1 design (data-model.md, contracts/, quickstart.md).
