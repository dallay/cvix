# Tasks: Resume Generator MVP

**Input**: Design documents from `/specs/003-resume-generator-mvp/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/resume-api.yaml ✅

**Tests**: Per Constitution Principle II, tests MUST be written BEFORE implementation code (TDD). All tests for User Story 1 are positioned in Phase 3 BEFORE implementation tasks.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Backend: `server/engine/src/main/kotlin/com/loomify/resume/`
- Backend tests: `server/engine/src/test/kotlin/com/loomify/resume/`
- Frontend: `client/apps/webapp/src/resume/`
- Frontend tests: `client/apps/webapp/src/__tests__/resume/`
- Resources: `server/engine/src/main/resources/`
- E2E tests: `client/e2e/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create domain package structure in `server/engine/src/main/kotlin/com/loomify/resume/domain/`
- [X] T002 Create application package structure in `server/engine/src/main/kotlin/com/loomify/resume/application/`
- [X] T003 Create infrastructure package structure in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/`
- [X] T004 [P] Create test directory structure in `server/engine/src/test/kotlin/com/loomify/resume/`
- [X] T005 [P] Create frontend feature structure in `client/apps/webapp/src/resume/`
- [X] T006 [P] Add StringTemplate 4 dependency to `server/engine/build.gradle.kts`
- [X] T007 [P] Pull TeX Live Docker image and verify availability (see quickstart.md)
- [X] T008 [P] Create LaTeX template resources directory in `server/engine/src/main/resources/templates/resume/`
- [X] T009 [P] Create i18n messages directory in `server/engine/src/main/resources/messages/`
- [X] T010 [P] Create example JSON payloads directory in `specs/003-resume-generator-mvp/examples/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T011 [P] Use reusable Email value object in shared common library (`com.loomify.common.domain.vo.email`)
- [X] T012 [P] Create domain exception hierarchy in `server/engine/src/main/kotlin/com/loomify/resume/domain/exception/ResumeGenerationException.kt`
- [X] T013 [P] Define PdfGeneratorPort interface in `server/engine/src/main/kotlin/com/loomify/resume/domain/port/PdfGeneratorPort.kt`
- [X] T014 [P] Define TemplateRendererPort interface in `server/engine/src/main/kotlin/com/loomify/resume/domain/port/TemplateRendererPort.kt`
- [X] T015 [P] Create Docker configuration class in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerConfiguration.kt`
- [X] T016 [P] Create StringTemplate configuration in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/template/StringTemplateConfiguration.kt`
- [X] T017 [P] Create GlobalExceptionHandler in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/GlobalExceptionHandler.kt`
- [X] T018 [P] Create ErrorResponse DTO in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/dto/ErrorResponse.kt`
- [X] T019 Create base LaTeX template structure (English) in `server/engine/src/main/resources/templates/resume/resume-template-en.tex`
- [X] T020 Create base LaTeX template structure (Spanish) in `server/engine/src/main/resources/templates/resume/resume-template-es.tex`
- [X] T021 [P] Create English i18n messages in `server/engine/src/main/resources/messages/messages.properties`
- [X] T022 [P] Create Spanish i18n messages in `server/engine/src/main/resources/messages/messages_es.properties`
- [X] T023 [P] Create TypeScript types for JSON Resume schema in `client/apps/webapp/src/resume/types/resume.ts`
- [X] T024 [P] Create Zod validation schema in `client/apps/webapp/src/resume/schemas/resumeSchema.ts`
- [X] T025 [P] Add English translations in `client/apps/webapp/src/i18n/locales/en/resume.json`
- [X] T026 [P] Add Spanish translations in `client/apps/webapp/src/i18n/locales/es/resume.json`
- [X] T027A [P] Configure Bucket4j dependencies in `server/engine/engine.gradle.kts` (reuse from auth module per plan.md)
- [ ] T027B [P] Implement @RateLimiting annotation pattern on resume endpoints (10 requests/minute per spec.md) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Basic Resume Generation (Priority: P1) 🎯 MVP

**Goal**: Enable users to fill out a web form with resume data and receive a professionally formatted PDF within 8 seconds

**Independent Test**: Fill form with sample data (software-engineer.json), click generate, verify PDF downloads with correct formatting

### Tests for User Story 1 (TDD - Write Tests FIRST) ✅

**CRITICAL**: Per Constitution Principle II, tests MUST be written BEFORE implementation code. All tests below should FAIL initially.

#### Unit Tests (Backend) - Write FIRST, verify they FAIL

- [ ] T028 [P] [US1] Unit test for ResumeData validation logic (must have work/education/skills) in `server/engine/src/test/kotlin/com/loomify/resume/domain/model/ResumeDataTest.kt`
- [ ] T029 [P] [US1] Unit test for WorkExperience date validation (endDate >= startDate) in `server/engine/src/test/kotlin/com/loomify/resume/domain/model/WorkExperienceTest.kt`
- [ ] T030 [P] [US1] Unit test for Education date validation in `server/engine/src/test/kotlin/com/loomify/resume/domain/model/EducationTest.kt`
- [ ] T031 [P] [US1] Unit test for PersonalInfo value object validations (name, email) in `server/engine/src/test/kotlin/com/loomify/resume/domain/model/PersonalInfoTest.kt`
- [ ] T032 [P] [US1] Unit test for SkillCategory validation (must have keywords) in `server/engine/src/test/kotlin/com/loomify/resume/domain/model/SkillCategoryTest.kt`
- [ ] T033 [P] [US1] Unit test for GenerateResumeCommandHandler orchestration logic in `server/engine/src/test/kotlin/com/loomify/resume/application/handler/GenerateResumeCommandHandlerTest.kt`

#### Integration Tests (Backend) - Write FIRST, verify they FAIL

- [ ] T034 [P] [US1] Integration test for ResumeController POST /api/resumes endpoint (@WebFluxTest) in `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/web/ResumeControllerTest.kt`
- [ ] T035 [P] [US1] Integration test for DockerPdfGeneratorAdapter with Testcontainers in `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapterTest.kt`
- [ ] T036 [P] [US1] Integration test for LatexTemplateRenderer with sample data in `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/template/LatexTemplateRendererTest.kt`

#### Component Tests (Frontend) - Write FIRST, verify they FAIL

- [ ] T037 [P] [US1] Component test for ResumeForm.vue (form validation, submission) in `client/apps/webapp/src/__tests__/resume/ResumeForm.spec.ts`
- [ ] T038 [P] [US1] Composable test for useResumeGeneration (API calls, error handling) in `client/apps/webapp/src/__tests__/resume/useResumeGeneration.spec.ts`
- [ ] T039 [P] [US1] Component test for PersonalInfoSection.vue in `client/apps/webapp/src/__tests__/resume/PersonalInfoSection.spec.ts`

**Checkpoint**: All tests written and FAILING. Now proceed with implementation to make them pass.

### Implementation for User Story 1

#### Domain Layer (Backend)

- [ ] T040 [P] [US1] Create FullName value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/PersonalInfo.kt`
- [ ] T041 [P] [US1] Create JobTitle value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/PersonalInfo.kt`
- [ ] T042 [P] [US1] Create PhoneNumber value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/PersonalInfo.kt`
- [ ] T043 [P] [US1] Create Url value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/PersonalInfo.kt`
- [ ] T044 [P] [US1] Create Summary value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/PersonalInfo.kt`
- [ ] T045 [P] [US1] Create Location and SocialProfile data classes in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/PersonalInfo.kt`
- [ ] T046 [US1] Create PersonalInfo value object (depends on T040-T045) in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/PersonalInfo.kt`
- [ ] T047 [P] [US1] Create CompanyName value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/WorkExperience.kt`
- [ ] T048 [P] [US1] Create Highlight value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/WorkExperience.kt`
- [ ] T049 [US1] Create WorkExperience entity with validation and formatPeriod logic in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/WorkExperience.kt`
- [ ] T050 [P] [US1] Create InstitutionName value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/Education.kt`
- [ ] T051 [P] [US1] Create FieldOfStudy value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/Education.kt`
- [ ] T052 [P] [US1] Create DegreeType value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/Education.kt`
- [ ] T053 [US1] Create Education entity with validation and formatPeriod logic in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/Education.kt`
- [ ] T054 [P] [US1] Create SkillCategoryName value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/SkillCategory.kt`
- [ ] T055 [P] [US1] Create Skill value object in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/SkillCategory.kt`
- [ ] T056 [US1] Create SkillCategory entity in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/SkillCategory.kt`
- [ ] T057 [P] [US1] Create Language entity in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/Language.kt`
- [ ] T058 [P] [US1] Create Project entity in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/Project.kt`
- [ ] T059 [US1] Create ContentMetrics data class in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/ResumeData.kt`
- [ ] T060 [US1] Create ResumeData aggregate root with validation (depends on T046, T049, T053, T056-T058) in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/ResumeData.kt`
- [ ] T061 [US1] Create GeneratedDocument domain event in `server/engine/src/main/kotlin/com/loomify/resume/domain/event/GeneratedDocument.kt`

#### Application Layer (Backend)

- [ ] T062 [US1] Create GenerateResumeCommand in `server/engine/src/main/kotlin/com/loomify/resume/application/command/GenerateResumeCommand.kt`
- [ ] T063 [US1] Implement GenerateResumeCommandHandler with CQRS pattern (depends on T062, port interfaces, domain entities) in `server/engine/src/main/kotlin/com/loomify/resume/application/handler/GenerateResumeCommandHandler.kt`

#### Infrastructure Layer (Backend)

- [ ] T064 [US1] Implement LatexTemplateRenderer adapter using StringTemplate 4 with escapeLatex() utility (depends on T014) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/template/LatexTemplateRenderer.kt`
- [ ] T065 [US1] Implement LaTeX injection prevention - escapeLatex() function for special chars (\, {, }, $, &, %, #, _, ^, ~) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/template/LatexTemplateRenderer.kt`
- [ ] T066 [US1] Implement DockerPdfGeneratorAdapter base with Docker Java Library (depends on T013, T015) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T067 [US1] Add Docker security restrictions (--read-only, --no-new-privileges, --security-opt=no-new-privileges:true) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T068 [US1] Add Docker network isolation (restricted network access) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T069 [US1] Implement container lifecycle management and cleanup logic in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T070 [US1] Add semaphore-based concurrency throttling (max 10 containers) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T071 [US1] Implement 10-second timeout handling in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T072 [US1] Add Docker resource limits (512MB memory, 0.5 CPU) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T073 [US1] Create GenerateResumeRequest DTO in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/dto/GenerateResumeRequest.kt`
- [ ] T074 [US1] Implement ResumeController POST /api/resumes endpoint with JWT auth (depends on T063, T073) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`
- [ ] T075 [US1] Add Content-Disposition, Content-Length, X-Generation-Time-Ms headers to response in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`
- [ ] T076 [US1] Add Accept-Language header handling for locale selection in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`
- [ ] T077 [US1] Add API-Version header validation in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`

#### LaTeX Template (Backend)

- [ ] T064 [US1] Implement adaptive layout logic in English template using ifthen/etoolbox (depends on T019) in `server/engine/src/main/resources/templates/resume/resume-template-en.tex`
- [ ] T065 [US1] Implement adaptive layout logic in Spanish template using ifthen/etoolbox (depends on T020) in `server/engine/src/main/resources/templates/resume/resume-template-es.tex`
- [ ] T066 [US1] Add section templates for PersonalInfo, WorkExperience, Education, Skills, Languages, Projects to English template in `server/engine/src/main/resources/templates/resume/resume-template-en.tex`
- [ ] T067 [US1] Add section templates for PersonalInfo, WorkExperience, Education, Skills, Languages, Projects to Spanish template in `server/engine/src/main/resources/templates/resume/resume-template-es.tex`
- [ ] T068 [US1] Add conditional section ordering based on content metrics in both templates in `server/engine/src/main/resources/templates/resume/resume-template-en.tex` and `resume-template-es.tex`

#### Frontend (Form UI)

- [ ] T069 [P] [US1] Create PersonalInfoSection.vue component in `client/apps/webapp/src/resume/components/PersonalInfoSection.vue`
- [ ] T070 [P] [US1] Create WorkExperienceSection.vue component (with dynamic add/remove entries) in `client/apps/webapp/src/resume/components/WorkExperienceSection.vue`
- [ ] T071 [P] [US1] Create EducationSection.vue component (with dynamic add/remove entries) in `client/apps/webapp/src/resume/components/EducationSection.vue`
- [ ] T072 [P] [US1] Create SkillsSection.vue component (with dynamic add/remove categories) in `client/apps/webapp/src/resume/components/SkillsSection.vue`
- [ ] T073 [P] [US1] Create LanguagesSection.vue component (optional section) in `client/apps/webapp/src/resume/components/LanguagesSection.vue`
- [ ] T074 [P] [US1] Create ProjectsSection.vue component (optional section) in `client/apps/webapp/src/resume/components/ProjectsSection.vue`
- [ ] T075 [US1] Create ResumeForm.vue main component integrating all sections (depends on T069-T074) in `client/apps/webapp/src/resume/components/ResumeForm.vue`
- [ ] T076 [US1] Implement session-based form data persistence using sessionStorage in `client/apps/webapp/src/resume/composables/useResumeSession.ts`
- [ ] T077 [US1] Create useResumeGeneration composable with API call logic in `client/apps/webapp/src/resume/composables/useResumeGeneration.ts`
- [ ] T078 [US1] Create Pinia store for resume state management in `client/apps/webapp/src/resume/stores/resumeStore.ts`
- [ ] T079 [US1] Create API client for resume endpoint in `client/apps/webapp/src/api/resume.ts`
- [ ] T080 [US1] Create ResumeGeneratorPage.vue main page (depends on T075, T077-T079) in `client/apps/webapp/src/resume/pages/ResumeGeneratorPage.vue`
- [ ] T081 [US1] Add route for resume generator page to Vue Router in `client/apps/webapp/src/router/index.ts`
- [ ] T082 [US1] Add loading spinner/progress indicator during PDF generation in `client/apps/webapp/src/resume/components/ResumeForm.vue`

#### Example Data

- [ ] T083 [P] [US1] Create software-engineer.json example payload in `specs/003-resume-generator-mvp/examples/software-engineer.json`
- [ ] T084 [P] [US1] Create project-manager.json example payload in `specs/003-resume-generator-mvp/examples/project-manager.json`
- [ ] T085 [P] [US1] Create minimal.json example payload in `specs/003-resume-generator-mvp/examples/minimal.json`

**Checkpoint**: User Story 1 complete - Users can generate PDF resumes via web form

---

## Phase 4: User Story 2 - Form Validation and Error Handling (Priority: P1)

**Goal**: Provide clear, immediate feedback when users make mistakes or skip required fields

**Independent Test**: Submit incomplete form (missing email), verify field-specific error message appears; enter invalid email, verify error on blur; correct errors and verify they disappear

### Implementation for User Story 2

- [ ] T086 [US2] Create useResumeValidation composable with vee-validate integration in `client/apps/webapp/src/resume/composables/useResumeValidation.ts`
- [ ] T087 [US2] Add Zod validation rules for all required fields in `client/apps/webapp/src/resume/schemas/resumeSchema.ts`
- [ ] T088 [US2] Add field length validation (100/500/50 char limits) in `client/apps/webapp/src/resume/schemas/resumeSchema.ts`
- [ ] T089 [US2] Implement manual validation on blur (no validate-on-blur/change/input props per conventions) in `client/apps/webapp/src/resume/components/PersonalInfoSection.vue`
- [ ] T090 [US2] Add validation error messages to all form sections in `client/apps/webapp/src/resume/components/WorkExperienceSection.vue`
- [ ] T091 [US2] Add validation error messages to all form sections in `client/apps/webapp/src/resume/components/EducationSection.vue`
- [ ] T092 [US2] Add validation error messages to all form sections in `client/apps/webapp/src/resume/components/SkillsSection.vue`
- [ ] T093 [US2] Implement date range validation (startDate <= endDate) in `client/apps/webapp/src/resume/schemas/resumeSchema.ts`
- [ ] T094 [US2] Add backend validation with Spring Validation annotations in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/dto/GenerateResumeRequest.kt`
- [ ] T095 [US2] Create custom validator for resume content requirement (at least one of: work, education, skills) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/validation/ResumeDataValidator.kt`
- [ ] T096 [US2] Update GlobalExceptionHandler to handle validation errors (HTTP 400) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/GlobalExceptionHandler.kt`
- [ ] T097 [US2] Add localized validation error messages to i18n files in `server/engine/src/main/resources/messages/messages.properties` and `messages_es.properties`
- [ ] T098 [US2] Implement payload size validation (<100KB) in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`

**Checkpoint**: User Story 2 complete - Form validation prevents bad submissions and guides users to fix errors

---

## Phase 5: User Story 3 - Real-time Preview (Priority: P2)

**Goal**: Show users how their resume will look before generating final PDF

**Independent Test**: Fill in form fields, observe preview panel updates within 500ms; modify existing data, verify preview reflects changes

### Implementation for User Story 3

- [ ] T099 [US3] Create ResumePreview.vue component with HTML mock-up of PDF layout in `client/apps/webapp/src/resume/components/ResumePreview.vue`
- [ ] T100 [US3] Add reactive watcher to update preview on form data changes in `client/apps/webapp/src/resume/components/ResumePreview.vue`
- [ ] T101 [US3] Implement debounced update (500ms) to avoid excessive re-renders in `client/apps/webapp/src/resume/components/ResumePreview.vue`
- [ ] T102 [US3] Create CSS styles matching LaTeX template appearance (typography, spacing, sections) in `client/apps/webapp/src/resume/components/ResumePreview.vue`
- [ ] T103 [US3] Add preview panel to ResumeGeneratorPage.vue with responsive layout in `client/apps/webapp/src/resume/pages/ResumeGeneratorPage.vue`
- [ ] T104 [US3] Implement conditional section rendering based on filled data in `client/apps/webapp/src/resume/components/ResumePreview.vue`
- [ ] T105 [US3] Add toggle button to show/hide preview panel in `client/apps/webapp/src/resume/pages/ResumeGeneratorPage.vue`

**Checkpoint**: User Story 3 complete - Users can preview resume layout before generating PDF

---

## Phase 6: User Story 4 - Mobile-Responsive Experience (Priority: P2)

**Goal**: Enable users to complete form and generate resume from mobile devices

**Independent Test**: Access site on 375px viewport, complete form, verify all fields accessible and PDF downloads correctly

### Implementation for User Story 4

- [ ] T106 [US4] Add mobile-responsive breakpoints to ResumeForm.vue using Tailwind responsive classes in `client/apps/webapp/src/resume/components/ResumeForm.vue`
- [ ] T107 [US4] Optimize form field sizing for touch targets (44px min height) in all section components in `client/apps/webapp/src/resume/components/PersonalInfoSection.vue`
- [ ] T108 [US4] Add appropriate input types for mobile keyboards (email, tel, url) in `client/apps/webapp/src/resume/components/PersonalInfoSection.vue`
- [ ] T109 [US4] Make preview panel collapsible/scrollable on mobile in `client/apps/webapp/src/resume/components/ResumePreview.vue`
- [ ] T110 [US4] Test mobile PDF download flow and handle mobile browser differences in `client/apps/webapp/src/resume/composables/useResumeGeneration.ts`
- [ ] T111 [US4] Add mobile-optimized layout for ResumeGeneratorPage (stacked vs side-by-side) in `client/apps/webapp/src/resume/pages/ResumeGeneratorPage.vue`

**Checkpoint**: User Story 4 complete - Resume generation works seamlessly on mobile devices

---

## Phase 7: User Story 5 - Error Recovery and Retry (Priority: P3)

**Goal**: Provide clear information and retry options when PDF generation fails

**Independent Test**: Simulate timeout error, verify clear error message and retry button; click retry, verify form data retained

### Implementation for User Story 5

- [ ] T112 [US5] Add error handling for LaTeX compilation errors (HTTP 422) in GlobalExceptionHandler in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/GlobalExceptionHandler.kt`
- [ ] T113 [US5] Add error handling for timeout errors (HTTP 504) in GlobalExceptionHandler in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/GlobalExceptionHandler.kt`
- [ ] T114 [US5] Add error handling for Docker execution errors (HTTP 500) in GlobalExceptionHandler in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/GlobalExceptionHandler.kt`
- [ ] T115 [US5] Create user-friendly error messages (no technical details exposed) in i18n files in `server/engine/src/main/resources/messages/messages.properties` and `messages_es.properties`
- [ ] T116 [US5] Add error state management to Pinia store in `client/apps/webapp/src/resume/stores/resumeStore.ts`
- [ ] T117 [US5] Create ErrorDisplay component with retry button in `client/apps/webapp/src/resume/components/ErrorDisplay.vue`
- [ ] T118 [US5] Implement retry logic that preserves form data in `client/apps/webapp/src/resume/composables/useResumeGeneration.ts`
- [ ] T119 [US5] Add specific error messages for different failure types (timeout, compilation, Docker) in `client/apps/webapp/src/resume/components/ErrorDisplay.vue`
- [ ] T120 [US5] Add structured logging for errors with anonymized identifiers in `server/engine/src/main/kotlin/com/loomify/resume/application/handler/GenerateResumeCommandHandler.kt`

**Checkpoint**: User Story 5 complete - Users can recover from errors without losing data

---

## Phase 8: Rate Limiting & Security (Cross-Cutting)

**Purpose**: Enforce rate limits and security requirements across all user stories

- [ ] T121 Implement rate limiting endpoint decorator using Bucket4j in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`
- [ ] T122 Add HTTP 429 response with Retry-After, X-RateLimit-* headers in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/GlobalExceptionHandler.kt`
- [ ] T123 Add rate limit exceeded error messages (localized) in `server/engine/src/main/resources/messages/messages.properties` and `messages_es.properties`
- [ ] T124 Display rate limit error with countdown timer on frontend in `client/apps/webapp/src/resume/components/ErrorDisplay.vue`
- [ ] T125 Add audit logging for all generation attempts in `server/engine/src/main/kotlin/com/loomify/resume/application/handler/GenerateResumeCommandHandler.kt`
- [ ] T133 Configure HTTPS termination and enforce HSTS header (Strict-Transport-Security) in production environment configuration
- [ ] T134 Set core security headers in controller responses: Content-Security-Policy (baseline), X-Content-Type-Options, X-Frame-Options, Referrer-Policy in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`
- [ ] T135 Add integration test to verify security headers present on resume endpoints in `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/web/ResumeControllerTest.kt`
- [ ] T136 Add test to verify PDFs are not persisted (stream-only, no disk writes) in `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapterTest.kt`
- [ ] T137 Add test to verify temp file cleanup on success and failure paths in `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapterTest.kt`
- [ ] T138 Verify Docker adapter enforces 10s timeout and surfaces timeout errors to metrics/logs in `server/engine/src/test/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapterTest.kt`

---

## Phase 9: Monitoring & Observability (Cross-Cutting)

**Purpose**: Add monitoring, metrics, and health checks

**Performance Targets**:

- PDF generation: 8 seconds (per spec.md Section 5.2)
- API endpoints: 200ms p95 (per Constitution VI)

- [ ] T126 [P] Add Spring Boot Actuator health endpoint for resume service in `server/engine/src/main/resources/application.yml`
- [ ] T127 [P] Add Docker availability check to health endpoint in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerHealthIndicator.kt`
- [ ] T128 [P] Add Prometheus metrics for PDF generation (count, duration, errors) using Micrometer - target: 8s in `server/engine/src/main/kotlin/com/loomify/resume/application/handler/GenerateResumeCommandHandler.kt`
- [ ] T129 [P] Add metrics for Docker container lifecycle events in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/pdf/DockerPdfGeneratorAdapter.kt`
- [ ] T130 [P] Add correlation IDs to all log statements in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/web/ResumeController.kt`
- [ ] T131 [P] Add Micrometer SLO metric for API p95 latency ≤200ms (excluding PDF step); create Prometheus alert at 200ms warn/250ms critical
- [ ] T132 [P] Expose error-rate metric (failures/total) with Prometheus counter; create alert when error rate >3%
- [ ] T139 [P] Add external uptime monitoring probe for 99.5% uptime SLO; document operator runbook for incidents
- [ ] T140 [P] Create Prometheus alerting rules and Grafana dashboard for PDF generation SLA (warn at >8s, critical at >=10s)
- [ ] T141 [P] Add metrics for concurrent request queueing and Docker container pool utilization

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T142 [P] Run accessibility audit (axe/testing-library a11y checks) on ResumeForm and Preview; fix WCAG AA issues in `client/apps/webapp/src/resume/components/`
- [ ] T143 [P] Add integration tests to verify semantic labels, keyboard focus order, ARIA roles, and screen reader text in `client/apps/webapp/src/__tests__/features/resume/`
- [ ] T144 [P] Document supported browsers (past 2 years) and add browser compatibility matrix to README
- [ ] T145 [P] Add test to verify double-submit debounce/throttle (frontend) and concurrent request handling (backend)
- [ ] T146 [P] Add KDoc documentation to all domain entities in `server/engine/src/main/kotlin/com/loomify/resume/domain/model/`
- [ ] T147 [P] Add JSDoc documentation to TypeScript types in `client/apps/webapp/src/resume/types/resume.ts`
- [ ] T148 [P] Generate OpenAPI documentation from SpringDoc annotations in `server/engine/build.gradle.kts`
- [ ] T149 Update main README.md with resume generator feature documentation in `README.md`
- [ ] T150 Run Detekt and fix any violations in `server/engine/src/main/kotlin/com/loomify/resume/`
- [ ] T151 Run Biome and fix any violations in `client/apps/webapp/src/resume/`
- [ ] T152 Validate quickstart.md by following all setup steps in `specs/003-resume-generator-mvp/quickstart.md`
- [ ] T153 Test with all three example payloads (software-engineer, project-manager, minimal) and verify PDF output quality

---

## Phase 11: End-to-End Testing & Coverage Validation

**Purpose**: Final validation of the complete system and test coverage metrics

**⚠️ CRITICAL**: This phase must be executed LAST, after all other phases are complete

- [ ] T154 E2E test for complete resume generation flow (Playwright) in `client/e2e/resume-generation.spec.ts` - Run against staging environment per Constitution Principle II
- [ ] T155 Verify 80% backend test coverage using Kover report
- [ ] T156 Verify 75% frontend test coverage using Vitest coverage report
- [ ] T157 Verify 100% domain layer coverage (server/engine/src/main/kotlin/com/loomify/resume/domain/**) using Kover filtered report

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User Story 1 (Phase 3): Basic generation - no dependencies on other stories
  - User Story 2 (Phase 4): Validation - integrates with US1 form but can be tested independently
  - User Story 3 (Phase 5): Preview - integrates with US1 form but can be tested independently
  - User Story 4 (Phase 6): Mobile - enhances US1-3 but can be tested independently
  - User Story 5 (Phase 7): Error recovery - enhances US1 but can be tested independently
- **Rate Limiting (Phase 8)**: Depends on US1 controller implementation
- **Monitoring (Phase 9)**: Depends on US1 core implementation
- **Polish (Phase 10)**: Depends on all user stories being complete
- **E2E Testing (Phase 11)**: Must be LAST - depends on all phases complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after US1 form components exist - Should be independent but shares form components
- **User Story 3 (P2)**: Can start after US1 form components exist - Should be independent, adds preview feature
- **User Story 4 (P2)**: Can start after US1-3 complete - Enhances existing components with responsive design
- **User Story 5 (P3)**: Can start after US1 complete - Enhances error handling, independent test

### Within Each User Story

- Domain entities before application handlers
- Application handlers before infrastructure adapters
- Infrastructure adapters before REST controller
- Backend API before frontend integration
- Core implementation before edge case handling
- Story complete before moving to next priority

### Parallel Opportunities

- **Phase 1 (Setup)**: All tasks marked [P] can run in parallel
- **Phase 2 (Foundational)**: All tasks marked [P] can run in parallel (within Phase 2)
- **Phase 3 (US1)**:
  - T028-T046: All domain value objects can be created in parallel
  - T069-T074: All form section components can be created in parallel
  - T083-T085: All example payloads can be created in parallel
- **Phase 4 (US2)**: T089-T092 validation for different sections can be done in parallel
- **Phase 9 (Monitoring)**: All tasks marked [P] can run in parallel
- **Phase 10 (Polish)**: Many documentation and test tasks marked [P] can run in parallel
- **Once Foundational completes**: Multiple developers can work on different user stories in parallel

---

## Parallel Example: User Story 1 (Domain Layer)

```bash
# Launch all value objects together:
Task: "Create FullName value object in PersonalInfo.kt" [T040]
Task: "Create JobTitle value object in PersonalInfo.kt" [T041]
Task: "Create PhoneNumber value object in PersonalInfo.kt" [T042]
Task: "Create Url value object in PersonalInfo.kt" [T043]
Task: "Create Summary value object in PersonalInfo.kt" [T044]
Task: "Create Location and SocialProfile in PersonalInfo.kt" [T045]

# Launch all form section components together:
Task: "Create PersonalInfoSection.vue component" [T069]
Task: "Create WorkExperienceSection.vue component" [T070]
Task: "Create EducationSection.vue component" [T071]
Task: "Create SkillsSection.vue component" [T072]
Task: "Create LanguagesSection.vue component" [T073]
Task: "Create ProjectsSection.vue component" [T074]
```

---

## Implementation Strategy

### MVP First (User Stories 1 & 2 Only)

User Stories 1 and 2 are both P1 priority and deliver the core value proposition:

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Basic Generation)
4. Complete Phase 4: User Story 2 (Validation)
5. Complete Phase 8: Rate Limiting & Security
6. Complete Phase 9: Monitoring
7. **STOP and VALIDATE**: Test US1 + US2 independently
8. Deploy/demo if ready

**MVP Scope**: 50 tasks (T001-T098 + T121-T132) delivering:

- Professional PDF resume generation from web form
- Complete validation and error feedback
- Rate limiting and security hardening
- Monitoring and health checks

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Has value but needs validation
3. Add User Story 2 → Test independently → Deploy/Demo (MVP!)
4. Add User Story 3 → Test independently → Deploy/Demo (Preview feature)
5. Add User Story 4 → Test independently → Deploy/Demo (Mobile support)
6. Add User Story 5 → Test independently → Deploy/Demo (Error recovery)
7. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (critical path)
2. Once Foundational is done:
   - Developer A: User Story 1 (Basic Generation)
   - Developer B: User Story 2 (Validation) - waits for T075 (ResumeForm) from Dev A
   - Developer C: LaTeX templates (T064-T068) - supports Dev A
3. After US1 + US2 complete (MVP):
   - Developer A: User Story 3 (Preview)
   - Developer B: User Story 4 (Mobile)
   - Developer C: User Story 5 (Error Recovery)
4. Stories complete and integrate independently

---

## Summary

- **Total Tasks**: 157
- **MVP Scope**: ~110 tasks (Phase 1, 2, 3, 4, 8, 9 including new security/coverage tasks)
- **User Story Breakdown**:
  - Setup & Foundational: 28 tasks (T001-T027B)
  - User Story 1 (P1): 58 tasks (T028-T085) - includes TDD tests (T028-T039) BEFORE implementation
  - User Story 2 (P1): 13 tasks (T086-T098)
  - User Story 3 (P2): 7 tasks (T099-T105)
  - User Story 4 (P2): 6 tasks (T106-T111)
  - User Story 5 (P3): 9 tasks (T112-T120)
  - Rate Limiting & Security: 16 tasks (T121-T125, T133-T138, T145)
  - Monitoring & Observability: 10 tasks (T126-T132, T139-T141)
  - Polish & Accessibility: 12 tasks (T142-T153)
  - E2E Testing & Coverage: 4 tasks (T154-T157)

- **TDD Compliance**: ✅ Tests (T028-T039) are positioned BEFORE implementation in Phase 3, per Constitution Principle II

- **Constitution Alignment**: ✅ API latency target normalized to 200ms p95, domain 100% coverage gate added, security headers enforced, a11y audit included

- **Parallel Opportunities**:
  - Setup: 7 of 10 tasks can run in parallel
  - Foundational: 16 of 18 tasks can run in parallel
  - US1 Tests: All 12 test tasks (T028-T039) can be written in parallel
  - US1 Domain: Multiple value objects/entities can be created in parallel
  - US1 Frontend: 6 section components can be created in parallel

- **Critical Path**: Setup → Foundational → US1 Tests (TDD) → US1 Domain → US1 Application → US1 Infrastructure → US1 Frontend → US2 Validation → Rate Limiting & Security → Monitoring → Polish & Accessibility → E2E Testing

- **Suggested MVP**: User Stories 1 + 2 + Rate Limiting & Security + Monitoring (110 tasks, ~2-3 weeks for 2-3 developers)

- **Format Validation**: ✅ All tasks follow the required checklist format:
  - `- [ ]` checkbox
  - Task ID (T001-T157, no duplicates)
  - [P] marker for parallelizable tasks
  - [Story] label for user story phases (US1-US5)
  - Clear description with file paths

**Next Action**: Begin Phase 1 (Setup) tasks T001-T010 to create project structure.
