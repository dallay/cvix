# Data Model: Resume Generator MVP

*Generated: 2025-11-01*
*Feature Spec: `specs/003-resume-generator-mvp/spec.md`*

## Overview

This document defines the domain entities, value objects, and relationships for the Resume Generator MVP feature. The data model follows the JSON Resume schema standard (<https://jsonresume.org/schema>) and adheres to Hexagonal Architecture principles (pure Kotlin domain layer with no framework dependencies).

## Domain Entities

### 1. ResumeData (Aggregate Root)

**Purpose**: Root entity representing a complete resume following JSON Resume schema.

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/model/ResumeData.kt`

```kotlin
package com.cvix.resume.domain.model

import java.util.UUID

/**
 * Aggregate root for resume data following JSON Resume schema v1.0.0.
 *
 * Validation rules enforced by value objects:
 * - At least one of: work, education, or skills must be present (FR-001)
 * - All field length limits enforced (FR-004)
 * - Total payload size <100KB (FR-004)
 */
data class ResumeData(
    val id: UUID = UUID.randomUUID(),
    val basics: Basics,
    val work: List<WorkExperience> = emptyList(),
    val education: List<Education> = emptyList(),
    val skills: List<SkillCategory> = emptyList(),
    val languages: List<Language> = emptyList(),
    val projects: List<Project> = emptyList()
) {
    init {
        require(hasContent()) { "Resume must have at least one of: work experience, education, or skills" }
    }

    private fun hasContent(): Boolean =
        work.isNotEmpty() || education.isNotEmpty() || skills.isNotEmpty()

    /**
     * Calculates content metrics for adaptive template logic (FR-008).
     * Used by template engine to determine section emphasis.
     */
    fun contentMetrics(): ContentMetrics = ContentMetrics(
        skillsCount = skills.flatMap { it.keywords }.size,
        experienceYears = work.sumOf { it.durationInYears() },
        experienceEntries = work.size,
        educationEntries = education.size
    )
}

/**
 * Metrics used for smart content adaptation in LaTeX template.
 */
data class ContentMetrics(
    val skillsCount: Int,
    val experienceYears: Double,
    val experienceEntries: Int,
    val educationEntries: Int
)
```

**Business Rules**:

- BR-001: Resume MUST contain at least one non-empty section (work, education, or skills)
- BR-002: Content metrics are calculated on-demand for template rendering

**Relationships**:

- Owns: 1 Basics (required)
- Owns: 0..n WorkExperience entries
- Owns: 0..n Education entries
- Owns: 0..n SkillCategory entries
- Owns: 0..n Language entries
- Owns: 0..n Project entries

---

### 2. Basics (Value Object)

**Purpose**: Basic personal information (JSON Resume `basics` section).

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/model/Basics.kt`

```kotlin
package com.cvix.resume.domain.model

import com.cvix.common.domain.vo.email.Email  // Reused from shared library

/**
 * Personal information value object.
 *
 * Validation rules:
 * - fullName: Required, max 100 chars (FR-004)
 * - label (job title): Optional, max 100 chars (FR-004)
 * - email: Required, valid email format (FR-003) - reuses shared Email VO
 * - summary: Optional, max 500 chars (FR-004)
 *
 * Note: FullName is resume-specific (unstructured single string per JSON Resume schema).
 * The shared Name VO (firstName + lastName) has different semantics for user profiles.
 */
data class Basics(
    val fullName: FullName,
    val label: JobTitle?,
    val email: Email,  // Reused from com.cvix.common.domain.vo.email
    val phone: PhoneNumber?,
    val url: Url?,
    val summary: Summary?,
    val location: Location?,
    val profiles: List<SocialProfile> = emptyList()
)

// Value objects for type safety and validation

/**
 * Full name as single unstructured string (JSON Resume "name" field).
 * Different from shared Name VO which uses structured firstName + lastName.
 */
@JvmInline
value class FullName(val value: String) {
    init {
        require(value.isNotBlank()) { "Full name cannot be blank" }
        require(value.length <= 100) { "Full name cannot exceed 100 characters" }
    }
}

@JvmInline
value class JobTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "Job title cannot be blank" }
        require(value.length <= 100) { "Job title cannot exceed 100 characters" }
    }
}

@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Phone number cannot be blank" }
    }
}

@JvmInline
value class Url(val value: String) {
    init {
        require(value.matches(URL_REGEX)) { "Invalid URL format" }
    }

    companion object {
        private val URL_REGEX = "^https?://[^\\s/$.?#].[^\\s]*$".toRegex()
    }
}

@JvmInline
value class Summary(val value: String) {
    init {
        require(value.isNotBlank()) { "Summary cannot be blank" }
        require(value.length <= 500) { "Summary cannot exceed 500 characters" }
    }
}

data class Location(
    val city: String?,
    val countryCode: String?  // ISO 3166-1 alpha-2
)

data class SocialProfile(
    val network: String,      // e.g., "LinkedIn", "GitHub"
    val url: Url
)
```

**Business Rules**:

- BR-003: Full name and email are mandatory (JSON Resume spec)
- BR-004: Email validation reuses shared `com.cvix.common.domain.vo.email.Email` (RFC-compliant, 320
  char limit)
- BR-005: URLs must be valid HTTP/HTTPS format

---

### 3. WorkExperience (Value Object)

**Purpose**: Work history entry (JSON Resume `work[]` section).

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/model/WorkExperience.kt`

```kotlin
package com.cvix.resume.domain.model

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Work experience entry.
 *
 * Validation rules:
 * - company: Required, max 100 chars (FR-004)
 * - position: Required, max 100 chars (FR-004)
 * - summary: Optional, max 500 chars (FR-004)
 * - startDate: Required, must be valid date (FR-003)
 * - endDate: Optional (null = "Present"), must be >= startDate (FR-003)
 */
data class WorkExperience(
    val company: CompanyName,
    val position: JobTitle,
    val startDate: LocalDate,
    val endDate: LocalDate?,  // null = "Present"
    val summary: Summary?,
    val highlights: List<Highlight> = emptyList()
) {
    init {
        if (endDate != null) {
            require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
        }
    }

    /**
     * Calculates duration in years (used for content metrics).
     */
    fun durationInYears(): Double {
        val end = endDate ?: LocalDate.now()
        val period = Period.between(startDate, end)
        return period.years + (period.months / 12.0)
    }

    /**
     * Formats work period for display in LaTeX template.
     * Example: "Jan 2020 - Present" (English), "Ene 2020 - Presente" (Spanish)
     */
    fun formatPeriod(locale: Locale): String {
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy", locale)
        val startStr = startDate.format(formatter)
        val endStr = endDate?.format(formatter) ?: when (locale.language) {
            "es" -> "Presente"
            else -> "Present"
        }
        return "$startStr - $endStr"
    }
}

@JvmInline
value class CompanyName(val value: String) {
    init {
        require(value.isNotBlank()) { "Company name cannot be blank" }
        require(value.length <= 100) { "Company name cannot exceed 100 characters" }
    }
}

@JvmInline
value class Highlight(val value: String) {
    init {
        require(value.isNotBlank()) { "Highlight cannot be blank" }
        require(value.length <= 500) { "Highlight cannot exceed 500 characters" }
    }
}
```

**Business Rules**:

- BR-006: Start date is mandatory
- BR-007: End date must be on or after start date
- BR-008: Null end date represents "Present" (still employed)
- BR-009: Duration calculation includes current employment (end date = today)

---

### 4. Education (Value Object)

**Purpose**: Education history entry (JSON Resume `education[]` section).

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/model/Education.kt`

```kotlin
package com.cvix.resume.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Education entry.
 *
 * Validation rules:
 * - institution: Required, max 100 chars (FR-004)
 * - area (field of study): Required, max 100 chars (FR-004)
 * - studyType (degree): Required, max 100 chars (FR-004)
 * - startDate: Required, must be valid date (FR-003)
 * - endDate: Optional (null = "In Progress"), must be >= startDate (FR-003)
 */
data class Education(
    val institution: InstitutionName,
    val area: FieldOfStudy,
    val studyType: DegreeType,
    val startDate: LocalDate,
    val endDate: LocalDate?  // null = "In Progress"
) {
    init {
        if (endDate != null) {
            require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
        }
    }

    /**
     * Formats education period for display in LaTeX template.
     */
    fun formatPeriod(locale: Locale): String {
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy", locale)
        val startStr = startDate.format(formatter)
        val endStr = endDate?.format(formatter) ?: when (locale.language) {
            "es" -> "En Curso"
            else -> "In Progress"
        }
        return "$startStr - $endStr"
    }
}

@JvmInline
value class InstitutionName(val value: String) {
    init {
        require(value.isNotBlank()) { "Institution name cannot be blank" }
        require(value.length <= 100) { "Institution name cannot exceed 100 characters" }
    }
}

@JvmInline
value class FieldOfStudy(val value: String) {
    init {
        require(value.isNotBlank()) { "Field of study cannot be blank" }
        require(value.length <= 100) { "Field of study cannot exceed 100 characters" }
    }
}

@JvmInline
value class DegreeType(val value: String) {
    init {
        require(value.isNotBlank()) { "Degree type cannot be blank" }
        require(value.length <= 100) { "Degree type cannot exceed 100 characters" }
    }
}
```

**Business Rules**:

- BR-010: Institution, area, and study type are mandatory
- BR-011: Null end date represents "In Progress" (currently studying)

---

### 5. SkillCategory (Value Object)

**Purpose**: Skill category with keywords (JSON Resume `skills[]` section).

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/model/SkillCategory.kt`

```kotlin
package com.cvix.resume.domain.model

/**
 * Skill category with associated keywords.
 *
 * Validation rules:
 * - name: Required, max 100 chars (FR-004)
 * - keywords: At least 1 required, each max 50 chars (FR-004)
 */
data class SkillCategory(
    val name: SkillCategoryName,
    val keywords: List<Skill>
) {
    init {
        require(keywords.isNotEmpty()) { "Skill category must have at least one keyword" }
    }
}

@JvmInline
value class SkillCategoryName(val value: String) {
    init {
        require(value.isNotBlank()) { "Skill category name cannot be blank" }
        require(value.length <= 100) { "Skill category name cannot exceed 100 characters" }
    }
}

@JvmInline
value class Skill(val value: String) {
    init {
        require(value.isNotBlank()) { "Skill cannot be blank" }
        require(value.length <= 50) { "Skill cannot exceed 50 characters" }
    }
}
```

**Business Rules**:

- BR-012: Each skill category must have at least one keyword
- BR-013: Individual skills limited to 50 characters for formatting

---

### 6. Language (Value Object)

**Purpose**: Language proficiency (JSON Resume `languages[]` section).

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/model/Language.kt`

```kotlin
package com.cvix.resume.domain.model

/**
 * Language proficiency entry.
 */
data class Language(
    val language: LanguageName,
    val fluency: Fluency
)

@JvmInline
value class LanguageName(val value: String) {
    init {
        require(value.isNotBlank()) { "Language name cannot be blank" }
    }
}

@JvmInline
value class Fluency(val value: String) {
    init {
        require(value.isNotBlank()) { "Fluency level cannot be blank" }
    }
}
```

---

### 7. Project (Value Object)

**Purpose**: Personal/professional project (JSON Resume `projects[]` section).

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/model/Project.kt`

```kotlin
package com.cvix.resume.domain.model

/**
 * Project entry.
 *
 * Validation rules:
 * - name: Required, max 100 chars (FR-004)
 * - description: Required, max 500 chars (FR-004)
 */
data class Project(
    val name: ProjectName,
    val description: ProjectDescription,
    val url: Url?
)

@JvmInline
value class ProjectName(val value: String) {
    init {
        require(value.isNotBlank()) { "Project name cannot be blank" }
        require(value.length <= 100) { "Project name cannot exceed 100 characters" }
    }
}

@JvmInline
value class ProjectDescription(val value: String) {
    init {
        require(value.isNotBlank()) { "Project description cannot be blank" }
        require(value.length <= 500) { "Project description cannot exceed 500 characters" }
    }
}
```

---

## Application Layer Entities

### 8. GenerateResumeCommand (Command)

**Purpose**: CQRS command encapsulating resume generation request.

**Location**:
`server/engine/src/main/kotlin/com/cvix/resume/application/command/GenerateResumeCommand.kt`

```kotlin
package com.cvix.resume.application.command

import com.cvix.resume.domain.model.ResumeData
import java.util.Locale
import java.util.UUID

/**
 * Command to generate a PDF resume.
 *
 * Validation:
 * - resumeData: Must be valid ResumeData aggregate
 * - locale: Supported locales: en, es (FR-005)
 * - userId: Required for rate limiting (FR-013)
 */
data class GenerateResumeCommand(
    val commandId: UUID = UUID.randomUUID(),
    val resumeData: ResumeData,
    val locale: Locale,
    val userId: String
) {
    init {
        require(locale.language in SUPPORTED_LANGUAGES) {
            "Unsupported locale: ${locale.language}. Supported: $SUPPORTED_LANGUAGES"
        }
    }

    companion object {
        val SUPPORTED_LANGUAGES = setOf("en", "es")
    }
}
```

**Business Rules**:

- BR-014: Only English and Spanish locales supported in MVP
- BR-015: User ID required for rate limiting and audit trail

---

### 9. GeneratedDocument (Domain Event)

**Purpose**: Result of resume generation operation.

**Location**: `server/engine/src/main/kotlin/com/cvix/resume/domain/event/GeneratedDocument.kt`

```kotlin
package com.cvix.resume.domain.event

import java.time.Instant
import java.util.UUID

/**
 * Domain event representing successful resume generation.
 */
data class GeneratedDocument(
    val eventId: UUID = UUID.randomUUID(),
    val commandId: UUID,
    val pdfBytes: ByteArray,
    val generatedAt: Instant = Instant.now(),
    val locale: java.util.Locale,
    val fileSizeBytes: Long = pdfBytes.size.toLong()
) {
    init {
        require(pdfBytes.isNotEmpty()) { "Generated PDF cannot be empty" }
        require(fileSizeBytes < MAX_PDF_SIZE_BYTES) {
            "Generated PDF exceeds maximum size: $fileSizeBytes > $MAX_PDF_SIZE_BYTES"
        }
    }

    companion object {
        const val MAX_PDF_SIZE_BYTES = 5 * 1024 * 1024 // 5MB
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GeneratedDocument
        return eventId == other.eventId
    }

    override fun hashCode(): Int = eventId.hashCode()
}
```

**Business Rules**:

- BR-016: Generated PDF cannot be empty
- BR-017: Generated PDF cannot exceed 5MB (reasonable limit for A4 resume with no images)

---

### 10. ResumeGenerationException (Domain Exception)

**Purpose**: Domain-specific exception hierarchy for resume generation failures.

**Location**:
`server/engine/src/main/kotlin/com/cvix/resume/domain/exception/ResumeGenerationException.kt`

```kotlin
package com.cvix.resume.domain.exception

/**
 * Base exception for resume generation failures.
 */
sealed class ResumeGenerationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Thrown when LaTeX compilation fails (syntax error, package missing, etc.).
 */
class LatexCompilationException(message: String, cause: Throwable? = null) : ResumeGenerationException(message, cause)

/**
 * Thrown when PDF generation exceeds timeout (>10s).
 */
class PdfGenerationTimeoutException(message: String) : ResumeGenerationException(message)

/**
 * Thrown when Docker container fails to start or crashes.
 */
class DockerExecutionException(message: String, cause: Throwable? = null) : ResumeGenerationException(message, cause)

/**
 * Thrown when template rendering fails (invalid data, missing variables).
 */
class TemplateRenderingException(message: String, cause: Throwable? = null) : ResumeGenerationException(message, cause)
```

---

## State Transitions

### Resume Generation Flow

```text
[Command Received]
    ↓
[Validate ResumeData] ─→ [Invalid] → HTTP 400 Bad Request
    ↓ Valid
[Check Rate Limit] ─→ [Exceeded] → HTTP 429 Too Many Requests
    ↓ Allowed
[Calculate Content Metrics]
    ↓
[Render LaTeX Template]
    ↓
[Spawn Docker Container]
    ↓
[Execute pdflatex] ─→ [Timeout >10s] → HTTP 504 Gateway Timeout
    ↓              ─→ [Compilation Error] → HTTP 422 Unprocessable Entity
    ↓ Success
[Extract PDF Bytes]
    ↓
[Cleanup Container]
    ↓
[Return GeneratedDocument] → HTTP 200 OK (application/pdf)
```

---

## Validation Rules Summary

| Rule ID | Entity                | Field       | Constraint                                                      |
| ------- |-----------------------| ----------- | --------------------------------------------------------------- |
| VR-001  | Basics                | fullName    | Required, max 100 chars                                         |
| VR-002  | Basics                | email       | Required, reuses shared Email VO (RFC-compliant, max 320 chars) |
| VR-003  | Basics                | label       | Optional, max 100 chars                                         |
| VR-004  | Basics                | summary     | Optional, max 500 chars                                         |
| VR-005  | WorkExperience        | company     | Required, max 100 chars                                         |
| VR-006  | WorkExperience        | position    | Required, max 100 chars                                         |
| VR-007  | WorkExperience        | summary     | Optional, max 500 chars                                         |
| VR-008  | WorkExperience        | startDate   | Required, must be <= endDate                                    |
| VR-009  | WorkExperience        | endDate     | Optional (null = "Present"), must be >= startDate               |
| VR-010  | Education             | institution | Required, max 100 chars                                         |
| VR-011  | Education             | area        | Required, max 100 chars                                         |
| VR-012  | Education             | studyType   | Required, max 100 chars                                         |
| VR-013  | Education             | startDate   | Required, must be <= endDate                                    |
| VR-014  | Education             | endDate     | Optional (null = "In Progress"), must be >= startDate           |
| VR-015  | SkillCategory         | name        | Required, max 100 chars                                         |
| VR-016  | SkillCategory         | keywords    | At least 1 required, each max 50 chars                          |
| VR-017  | Project               | name        | Required, max 100 chars                                         |
| VR-018  | Project               | description | Required, max 500 chars                                         |
| VR-019  | ResumeData            | aggregate   | Must have at least one of: work, education, or skills           |
| VR-020  | GenerateResumeCommand | locale      | Must be 'en' or 'es'                                            |
| VR-021  | GenerateResumeCommand | userId      | Required (non-blank string)                                     |
| VR-022  | GeneratedDocument     | pdfBytes    | Non-empty, <5MB                                                 |

---

## Persistence Strategy (Future)

**MVP Decision**: Resume generation is **stateless** - no database persistence of generated PDFs.

**If Persistence Added Later**:

- **Entity**: `GeneratedResume` (database entity, separate from domain `ResumeData`)
- **Table**: `generated_resumes` with columns:
  - `id` (UUID, primary key)
  - `user_id` (UUID, foreign key to users table)
  - `tenant_id` (UUID, for RLS multi-tenancy)
  - `resume_data_json` (JSONB, stores ResumeData as JSON Resume schema)
  - `pdf_s3_key` (VARCHAR, S3 object key for PDF storage)
  - `locale` (VARCHAR(5), e.g., "en", "es")
  - `created_at` (TIMESTAMP)
  - `file_size_bytes` (BIGINT)
- **Indexes**:
  - `idx_generated_resumes_user_id` on `user_id`
  - `idx_generated_resumes_tenant_id` on `tenant_id` (for RLS)
  - `idx_generated_resumes_created_at` on `created_at` (for cleanup jobs)
- **RLS Policy**: `tenant_id = current_setting('app.current_tenant')::uuid`

---

## Hexagonal Architecture Mapping

| Layer              | Component                                               | Location                   |
| ------------------ |---------------------------------------------------------| -------------------------- |
| **Domain**         | ResumeData, Basics, WorkExperience, etc.                | `domain/model/`            |
| **Domain**         | ResumeGenerationException hierarchy                     | `domain/exception/`        |
| **Domain**         | GeneratedDocument (event)                               | `domain/event/`            |
| **Domain**         | PdfGeneratorPort (interface)                            | `domain/port/`             |
| **Application**    | GenerateResumeCommand                                   | `application/command/`     |
| **Application**    | GenerateResumeCommandHandler                            | `application/handler/`     |
| **Infrastructure** | ResumeController (REST API)                             | `infrastructure/web/`      |
| **Infrastructure** | DockerPdfGeneratorAdapter (implements PdfGeneratorPort) | `infrastructure/pdf/`      |
| **Infrastructure** | LatexTemplateRenderer                                   | `infrastructure/template/` |

---

## Next Steps

1. ✅ Data model defined
2. ⏳ Create OpenAPI contract for REST endpoint (`contracts/resume-api.yaml`)
3. ⏳ Generate developer quickstart guide (`quickstart.md`)
4. ⏳ Update agent context with new domain entities

---

*This data model document will be used as the foundation for code generation in Phase 2 (tasks.md).*
