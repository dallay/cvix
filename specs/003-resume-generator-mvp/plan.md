# Implementation Plan: Resume Generator MVP

**Branch**: `003-resume-generator-mvp` | **Date**: October 31, 2025 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-resume-generator-mvp/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build a web-based resume generator that converts user-submitted form data into professionally formatted PDF resumes. The system uses a smart, adaptive LaTeX template following the JSON Resume schema standard to support diverse professional backgrounds. Users fill out a Vue.js-based form, the Spring Boot backend validates and processes the data through a secure LaTeX compilation pipeline in isolated Docker containers, and streams the generated PDF directly to the user. The MVP supports English and Spanish localization with intelligent content layout that adapts section emphasis based on user input.

## Technical Context

**Language/Version**: Kotlin 2.0.20+ (backend), TypeScript 5.x+ (frontend)

**Primary Dependencies**:

- Backend: Spring Boot 3.3.4+, Spring WebFlux, Spring Security OAuth2, R2DBC
- Frontend: Vue.js 3.5+, Vite 7+, Pinia 3+, Vue I18n, TailwindCSS 4+
- Template Engine: StringTemplate 4 or FreeMarker
- PDF Generation: TeX Live (pdflatex) in Docker
- Containerization: Docker + Alpine Linux

**Storage**: Session-based (no persistent storage), temporary file cleanup after generation

**Testing**: Backend: JUnit 5 + Kotest + Testcontainers + MockK, Frontend: Vitest + Testing Library Vue, E2E: Playwright

**Target Platform**: Web application (responsive design for desktop and mobile browsers)

**Project Type**: Web (monorepo with backend and frontend)

**Performance Goals**:

- PDF generation: <8 seconds (p95)
- API latency: <500ms (p95) excluding PDF generation
- Support 50 concurrent users without degradation

**Constraints**:

- Field length limits: names/titles (100 chars), descriptions (500 chars), skills (50 chars)
- Request payload: <100KB
- Rate limiting: 10 requests/minute/user
- Docker resource limits: 512MB memory, 0.5 CPU core, 10-second timeout

**Scale/Scope**:

- MVP: 50 concurrent users, 500 PDFs in first 30 days
- Single adaptive template supporting multiple industries
- Two languages (English/Spanish)
- Stateless architecture

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Hexagonal Architecture ✅ PASS

- **Domain layer**: Resume business entities (ResumeData, PersonalInfo, WorkExperience, etc.) will be pure Kotlin data classes with no framework dependencies
- **Application layer**: CQRS pattern - `GenerateResumeCommand` and `GenerateResumeCommandHandler` will orchestrate domain logic
- **Infrastructure layer**: All adapters isolated - REST controller (Spring WebFlux), LaTeX template engine (StringTemplate/FreeMarker), PDF generator (TeX Live Docker), JSON Resume validation

**Dependency Rule**: Domain ← Application ← Infrastructure. No violations.

### II. Test-Driven Development ✅ PASS

- **Coverage targets**: Backend 80%, Frontend 75% (specified in constitution)
- **Test strategy defined**:
  - Unit tests: Domain entity validation, template rendering, content adaptation logic
  - Integration tests: REST endpoint, Docker PDF generation, rate limiting
  - E2E tests: Complete resume generation flow (Playwright)
- **Testcontainers**: Will use PostgreSQL container for integration tests (if persistence added later)
- **MockK**: For mocking external dependencies (LaTeX engine, Docker API)

### III. Code Quality ✅ PASS

- **Backend**: Kotlin 2.0.20+, will follow `.ruler/01_BACKEND/01_KOTLIN_CONVENTIONS.md` (4 spaces, `val` over `var`, sealed classes for result types)
- **Frontend**: TypeScript strict mode, Biome for linting, Vue 3 Composition API with `<script setup lang="ts">`
- **Naming**: PascalCase classes, camelCase functions, UPPER_SNAKE_CASE constants
- **Documentation**: OpenAPI spec for REST endpoint, KDoc for domain models
- **Linting gates**: Detekt (backend), Biome (frontend) with zero violations before merge

### IV. Security ⚠️ REQUIRES ATTENTION

- **A01 Broken Access Control**: ✅ PASS - Rate limiting enforced (10 req/min/user), OAuth2 JWT validation required
- **A02 Cryptographic Failures**: ✅ PASS - No secrets stored; TLS for all API communication
- **A03 Injection**: ✅ PASS - LaTeX injection prevented via:
  - Input sanitization in application layer (escape special chars: `\`, `{`, `}`, `$`, `&`, `%`, `#`, `_`, `^`, `~`)
  - Template engine uses parameterized templates (no string concatenation)
  - Docker isolation provides defense-in-depth
- **A05 Security Misconfiguration**: ⚠️ **VIOLATION** - Docker container executes `pdflatex` on user input
  - **Mitigation**: Run Docker with `--read-only --no-new-privileges --security-opt=no-new-privileges:true`, restricted network access, 10-second timeout, resource limits (512MB RAM, 0.5 CPU)
  - **Justification**: PDF generation requires LaTeX compilation; Docker isolation minimizes risk
- **A06 Vulnerable Components**: ✅ PASS - TeX Live Alpine image, OWASP Dependency Check in CI
- **A08 Data Integrity**: ✅ PASS - JSON Resume schema validation with Zod

**Security Gate**: Passes with documented mitigation for Docker execution risk. See Complexity Tracking.

### V. User Experience ✅ PASS

- **Responsive Design**: Mobile-first approach (User Story 4: "mobile-optimized preview")
- **Accessibility**: WCAG 2.1 AA compliance - form labels, ARIA attributes, keyboard navigation, screen reader support
- **Internationalization**: Vue I18n for English/Spanish (form labels, error messages, date formats)
- **UI Components**: Shadcn-Vue (consistent with existing app)
- **Performance**: <8s PDF generation (FR-015), <500ms API response (p95 target from constitution)

### VI. Performance ✅ PASS WITH NOTES

**Backend Performance**:

- ✅ p95 response time: <500ms for API endpoints (LaTeX compilation offloaded to async Docker job)
- ✅ Rate limiting: 10 req/min/user prevents resource exhaustion
- ✅ Stateless design: Horizontal scaling ready
- ⚠️ **NOTE**: PDF generation (<8s per spec) exceeds p99 target (<500ms) but is justified by LaTeX compilation overhead

**Frontend Performance**:

- ✅ Lazy-load resume preview component with `defineAsyncComponent()`
- ✅ Code-split by route (Vue Router + Vite automatic)
- ✅ Bundle size: Minimal impact (form UI only, no heavy dependencies)
- ✅ FCP <1.5s, LCP <2.5s targets maintained

**Database Performance**:

- N/A - MVP is stateless (no database persistence for generated PDFs)
- If persistence added later: UUID v4 primary keys, indexed foreign keys, RLS for multi-tenancy

**Infrastructure Scalability**:

- ✅ Stateless service (no in-memory storage)
- ✅ Docker-based PDF generation enables horizontal scaling (spin up worker containers)
- ⚠️ **NOTE**: Docker container orchestration strategy needed (Phase 1 research)

### VII. Observability & Monitoring ✅ PASS

- **Logging**: Structured JSON logs with correlation IDs (Spring Boot Actuator)
- **Metrics**: Track request count, response times, error rates, Docker container lifecycle events
- **Health Checks**: Liveness (Spring Boot health endpoint), Readiness (Docker availability check)
- **Error Tracking**: Global `@ControllerAdvice` for consistent error responses (400, 422, 429, 500, 504)
- **API Documentation**: OpenAPI spec generated from code (SpringDoc)

### Technology Standards ⚠️ NEW TECHNOLOGIES

**Approved Stack**:

- ✅ Backend: Kotlin 2.0.20+, Spring Boot 3.3.4+, Spring WebFlux
- ✅ Frontend: Vue.js 3.5.17+, TypeScript 5.x, Vite 7.0.4+, Shadcn-Vue
- ✅ Testing: JUnit 5, Kotest, Testcontainers, MockK, Vitest, Playwright
- ✅ Containerization: Docker + Docker Compose

**New Technologies** (not in constitution):

- ⚠️ **TeX Live** (Alpine-based Docker image) - LaTeX distribution for PDF compilation
- ⚠️ **StringTemplate 4** or **FreeMarker** - Template engines for LaTeX generation
- ⚠️ **JSON Resume Schema** - Industry-standard resume data format

**Justification**: LaTeX is the industry standard for professional typesetting (academic CVs, technical resumes). Alternatives (HTML-to-PDF libraries like wkhtmltopdf, Puppeteer) lack fine-grained typography control required for print-quality output. StringTemplate/FreeMarker chosen for separation of template logic from business logic.

**Action**: Update constitution's Technology Standards section after this feature merges.

### Development Workflow ✅ PASS

- ✅ Branch: `feature/003-resume-generator-mvp` (already created)
- ✅ Commit messages: Conventional Commits with emojis
- ✅ Pre-commit: Biome, Detekt, changed files summary (Lefthook)
- ✅ Pre-push: Tests, builds, link checking (Lefthook)
- ✅ PR requirements: 1 approval, CI green, conversations resolved

### Quality Gates ✅ PASS

- ✅ Build Gate: `./gradlew build`, `pnpm run build` must succeed
- ✅ Test Gate: 80% backend coverage, 75% frontend coverage
- ✅ Quality Gate: Zero Detekt/Biome violations
- ✅ Security Gate: No high/critical vulnerabilities, secrets check passes
- ✅ Architecture Gate: Domain layer pure Kotlin, HTTP in infrastructure, repository interfaces
- ✅ Documentation Gate: OpenAPI spec, README updated with TeX Live setup

### Summary

**✅ PASSES Constitution Check** with documented exceptions:

1. Docker execution of user input (mitigated with isolation, timeouts, resource limits)
2. PDF generation latency (8s) exceeds p99 target (justified by LaTeX compilation)
3. New technologies (TeX Live, StringTemplate/FreeMarker, JSON Resume schema)

All exceptions are justified and documented in Complexity Tracking below.

## Project Structure

### Documentation (this feature)

```text
specs/003-resume-generator-mvp/
├── spec.md                     # Feature specification (business requirements)
├── plan.md                     # This file - technical implementation plan
├── research.md                 # Phase 0 research findings
├── data-model.md               # Domain entities and value objects
├── quickstart.md               # Developer onboarding guide
├── contracts/                  # API contracts
│   └── resume-api.yaml         # OpenAPI 3.0 specification
├── checklists/                 # Quality validation
│   └── requirements.md         # Requirements checklist
└── examples/                   # Sample JSON payloads (to be created)
    ├── software-engineer.json  # Skills-heavy resume
    ├── project-manager.json    # Experience-heavy resume
    └── minimal.json            # Bare minimum fields
```

### Source Code (repository root)

**Backend** (Spring Boot + Kotlin):

```text
server/engine/src/main/kotlin/com/loomify/resume/
├── domain/
│   ├── model/
│   │   ├── ResumeData.kt           # Aggregate root
│   │   ├── PersonalInfo.kt         # Value object
│   │   ├── WorkExperience.kt       # Value object
│   │   ├── Education.kt            # Value object
│   │   ├── SkillCategory.kt        # Value object
│   │   ├── Language.kt             # Value object
│   │   └── Project.kt              # Value object
│   ├── port/
│   │   ├── PdfGeneratorPort.kt     # Output port (interface)
│   │   └── TemplateRendererPort.kt # Output port (interface)
│   ├── event/
│   │   └── GeneratedDocument.kt    # Domain event
│   └── exception/
│       └── ResumeGenerationException.kt  # Exception hierarchy
├── application/
│   ├── command/
│   │   └── GenerateResumeCommand.kt      # CQRS command
│   └── handler/
│       └── GenerateResumeCommandHandler.kt  # Command handler
└── infrastructure/
    ├── web/
    │   ├── ResumeController.kt         # REST endpoint
    │   ├── dto/
    │   │   ├── GenerateResumeRequest.kt   # Request DTO
    │   │   └── ErrorResponse.kt           # Error DTO
    │   └── GlobalExceptionHandler.kt   # @ControllerAdvice
    ├── pdf/
    │   ├── DockerPdfGeneratorAdapter.kt    # Implements PdfGeneratorPort
    │   └── DockerConfiguration.kt          # Docker client config
    ├── template/
    │   ├── LatexTemplateRenderer.kt        # Implements TemplateRendererPort
    │   └── StringTemplateConfiguration.kt  # StringTemplate config
    └── validation/
        └── ResumeDataValidator.kt          # Custom validators

server/engine/src/main/resources/
├── templates/
│   └── resume/
│       ├── resume-template-en.tex  # English LaTeX template
│       └── resume-template-es.tex  # Spanish LaTeX template
└── messages/
    ├── messages.properties         # English i18n
    └── messages_es.properties      # Spanish i18n

server/engine/src/test/kotlin/com/loomify/resume/
├── domain/
│   └── model/
│       ├── ResumeDataTest.kt       # Unit tests
│       └── WorkExperienceTest.kt   # Unit tests
├── application/
│   └── handler/
│       └── GenerateResumeCommandHandlerTest.kt  # Unit tests
└── infrastructure/
    ├── web/
    │   └── ResumeControllerTest.kt     # Integration test (@WebFluxTest)
    └── pdf/
        └── DockerPdfGeneratorAdapterTest.kt  # Integration test (Testcontainers)
```

**Frontend** (Vue.js + TypeScript):

```text
client/apps/webapp/src/
├── features/
│   └── resume/
│       ├── components/
│       │   ├── ResumeForm.vue          # Main form component
│       │   ├── PersonalInfoSection.vue # Form section
│       │   ├── WorkExperienceSection.vue
│       │   ├── EducationSection.vue
│       │   ├── SkillsSection.vue
│       │   ├── LanguagesSection.vue
│       │   ├── ProjectsSection.vue
│       │   └── ResumePreview.vue       # PDF preview modal
│       ├── composables/
│       │   ├── useResumeGeneration.ts  # API calls
│       │   └── useResumeValidation.ts  # Form validation logic
│       ├── stores/
│       │   └── resumeStore.ts          # Pinia store
│       ├── types/
│       │   └── resume.ts               # TypeScript types (JSON Resume schema)
│       ├── schemas/
│       │   └── resumeSchema.ts         # Zod validation schema
│       └── pages/
│           ├── ResumeGeneratorPage.vue # Main page
│           └── ResumeListPage.vue      # Future: list of generated resumes
├── locales/
│   ├── en.json                         # English translations
│   └── es.json                         # Spanish translations
└── api/
    └── resume.ts                       # API client (axios)

client/apps/webapp/src/__tests__/
└── features/
    └── resume/
        ├── ResumeForm.spec.ts          # Component unit tests
        └── useResumeGeneration.spec.ts # Composable unit tests

client/e2e/
└── resume-generation.spec.ts           # E2E tests (Playwright)
```

**Infrastructure**:

```text
infra/
└── texlive/
    ├── Dockerfile                      # Custom TeX Live image (if needed)
    └── texlive.packages                # List of required LaTeX packages
```

**API Collection** (Bruno):

```text
endpoints/cvix/
└── resume/
    ├── generate-resume.bru             # POST /api/v1/resumes
    └── examples/
        ├── software-engineer.json      # Sample payload
        ├── project-manager.json
        └── minimal.json
```

**Structure Decision**: This is a **web application** (Option 2) with separate backend and frontend codebases. The backend follows Hexagonal Architecture (domain/application/infrastructure layers), and the frontend uses feature-based organization. All new code will be added to the existing `server/engine` and `client/apps/webapp` projects.

## Complexity Tracking

| Violation                                                   | Why Needed                                                                                                                                                                                   | Simpler Alternative Rejected Because                                                                                                                                                                                                                                                                                                                                           |
| ----------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Docker execution of user input** (Security)               | PDF generation requires LaTeX compilation (`pdflatex`) on user-provided resume data. LaTeX is the only viable option for print-quality typesetting with professional typography.             | **HTML-to-PDF libraries** (wkhtmltopdf, Puppeteer): Lack fine-grained typography control (kerning, ligatures, hyphenation). Cannot match LaTeX's mathematical typesetting and professional layout algorithms. **Server-side LaTeX** (without Docker): Exposes host system to potential code injection via LaTeX malicious packages. Docker provides mandatory isolation layer. |
| **PDF generation latency (8s)** (Performance)               | LaTeX compilation involves: (1) Parsing template, (2) Typesetting algorithms (line breaking, page layout), (3) Font rendering, (4) PDF generation. Industry benchmarks: 5-10s for A4 resume. | **Pre-rendered templates**: Cannot adapt to variable content length (skills-heavy vs experience-heavy). Spec requires "smart content adaptation" (FR-008). **Async background jobs**: Considered but rejected for MVP; adds complexity (job queue, polling UI). Users expect immediate feedback (<10s tolerance per UX research).                                              |
| **TeX Live + StringTemplate/FreeMarker** (New Technologies) | **TeX Live**: De facto standard for academic/technical documents (100MB Alpine image). **StringTemplate/FreeMarker**: Mature template engines with LaTeX-safe escaping.                      | **Custom template engine**: Reinventing the wheel; would take weeks to implement LaTeX escaping rules correctly. **Markdown-to-PDF**: Cannot achieve professional print quality required by spec. **Cloud PDF services** (DocRaptor, PDFShift): Vendor lock-in, costs scale linearly with usage, data privacy concerns.                                                        |
| **Docker container orchestration** (Infrastructure)         | Need strategy for: (1) Container lifecycle management, (2) Resource limits enforcement (512MB/0.5 CPU), (3) Timeout handling (10s), (4) Concurrent request queueing (50 users).              | **Direct pdflatex execution**: Security risk (no isolation). **Shared long-lived container**: Stateful (violates horizontal scaling); risk of resource exhaustion. **Kubernetes Jobs**: Over-engineered for MVP (50 concurrent users); adds operational complexity. Decision deferred to Phase 1 research.                                                                     |

---

## Implementation Plan Summary

### Phase 0: Research (Complete ✅)

**Status**: All technical unknowns resolved. See `research.md` for details.

**Key Findings**:

1. **LaTeX Adaptive Layouts**: Use `ifthen`/`etoolbox` packages with content metrics calculated in application layer
2. **JSON Resume Schema**: Kotlin domain entities map cleanly; use Spring Validation + Zod for validation
3. **Docker Orchestration**: Docker Java Library with semaphore-based throttling (10 concurrent containers)
4. **LaTeX i18n**: Separate templates per language (`resume-template-en.tex`, `resume-template-es.tex`)
5. **Rate Limiting**: Resilience4j RateLimiter for MVP, migrate to Redis-backed Bucket4j for production

### Phase 1: Design (Complete ✅)

**Status**: All design artifacts generated and validated.

**Deliverables**:

- ✅ `data-model.md`: 10 domain entities defined (ResumeData aggregate root + value objects)
- ✅ `contracts/resume-api.yaml`: OpenAPI 3.0 specification with full request/response schemas
- ✅ `quickstart.md`: Developer onboarding guide with setup instructions and test scenarios
- ✅ Agent context updated: `.github/copilot-instructions.md` includes TeX Live, StringTemplate, JSON Resume

**Architecture Decisions**:

- **Backend**: Hexagonal Architecture with CQRS (GenerateResumeCommand + Handler)
- **Frontend**: Feature-based organization with Pinia state management
- **PDF Generation**: Ephemeral Docker containers with StringTemplate for LaTeX rendering
- **Validation**: Multi-layer (Zod frontend, Spring Validation backend, domain invariants)

### Phase 2: Tasks Breakdown (Next Step 📋)

**Status**: Not started. Run `/speckit.tasks` command to generate detailed task breakdown.

**Expected Output**: `tasks.md` file with:

- Backend implementation tasks (domain entities, application handlers, infrastructure adapters)
- Frontend implementation tasks (components, composables, stores, pages)
- Testing tasks (unit, integration, E2E)
- Documentation tasks (KDoc, JSDoc, examples)
- Integration tasks (CI/CD, monitoring, deployment)

**Estimated Scope**:

- Backend: ~15-20 tasks (domain → application → infrastructure)
- Frontend: ~10-15 tasks (components → composables → pages)
- Testing: ~10-12 tasks (unit → integration → E2E)
- **Total**: ~35-47 tasks for MVP completion

### Critical Path

```text
1. Domain Entities (Backend)
   ↓
2. Template Rendering (Backend)
   ↓
3. Docker PDF Generator (Backend)
   ↓
4. Command Handler (Backend)
   ↓
5. REST Controller (Backend)
   ↓
6. Frontend Form Components
   ↓
7. API Integration (Frontend)
   ↓
8. E2E Tests
   ↓
9. Documentation & Deployment
```

### Success Criteria

**Before Merge to Main**:

- ✅ All 7 Phase 0 & Phase 1 planning tasks completed
- ⏳ 80%+ backend test coverage (unit + integration)
- ⏳ 75%+ frontend test coverage (unit + component)
- ⏳ E2E tests passing for critical flows (generate resume, rate limiting, validation errors)
- ⏳ Zero Detekt/Biome violations
- ⏳ OpenAPI spec validated and published
- ⏳ Quickstart guide tested by at least one other developer
- ⏳ Constitution Check re-validated (all gates still passing)

### Related Documents

- **Feature Spec**: `specs/003-resume-generator-mvp/spec.md`
- **Research Findings**: `specs/003-resume-generator-mvp/research.md`
- **Data Model**: `specs/003-resume-generator-mvp/data-model.md`
- **API Contract**: `specs/003-resume-generator-mvp/contracts/resume-api.yaml`
- **Quickstart**: `specs/003-resume-generator-mvp/quickstart.md`
- **Constitution**: `.specify/memory/constitution.md`

### Next Action

Run the following command to generate Phase 2 task breakdown:

```bash
/speckit.tasks
```

This will analyze the plan, data model, and API contract to produce a detailed, sequenced list of implementation tasks with dependencies and effort estimates.

---

**Plan Status**: ✅ Complete (Phases 0 & 1)
**Generated**: 2025-01-XX
**Last Updated**: 2025-01-XX
**Approved By**: [Pending Review]


