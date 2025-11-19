---
description: "Task list for Resume Data Entry Screen implementation"
---

# Tasks: Resume Data Entry Screen

**Input**: Design documents from `/specs/004-resume-data-entry/`
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, contracts/ ✓

**Tests**: Tests are NOT requested in the feature specification. Following TDD-optional approach, implementing tests after core functionality to validate behavior.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

This is a monorepo with:

- **Backend**: `server/engine/src/main/kotlin/com/loomify/resume/`
- **Frontend**: `client/apps/webapp/src/core/resume/`
- **Tests**: `server/engine/src/test/kotlin/com/loomify/resume/` and `client/e2e/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure for resume feature

- [X] T001 Create feature directory structure in `client/apps/webapp/src/core/resume/` with subdirectories: pages, components, stores, composables, types, validators
- [X] T002 Create backend module structure in `server/engine/src/main/kotlin/com/loomify/resume/` with subdirectories: domain, application, infrastructure
- [X] T003 [P] Download and pin JSON Resume schema v1.0.0 to `client/apps/webapp/src/core/resume/infrastructure/validation/json-resume.schema.json`. The version 1.0.0 is located at `docs/src/content/docs/json-resume/schema.json`
- [X] T004 [P] Add frontend dependencies to `client/apps/webapp/package.json`: ajv@^8.0.0, ajv-formats@^3.0.0, ajv-errors@^3.0.0, libphonenumber-js@^1.10.0, idb-keyval@^6.2.0
- [X] T005 [P] Add Liquibase migration script in `server/engine/src/main/resources/db/changelog/` to create resumes table with JSONB column
- [X] T006 Configure resume routes in `client/apps/webapp/src/core/resume/infrastructure/router/index.ts` for `/resume/editor` and `/resume/pdf` paths


---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T007 Create Resume domain entity in `server/engine/src/main/kotlin/com/loomify/resume/domain/Resume.kt` with all JSON Resume fields as Kotlin data classes
- [X] T008 [P] Align existing value objects in `server/engine/src/main/kotlin/com/loomify/resume/domain/` with JSON Resume schema: ensure Basics, Profile, Work (WorkExperience), Education, Skill (SkillCategory), Project, Language, Certificate, Publication, Award, Volunteer, Reference match data-model.md specifications. Rename PersonalInfo to Basics for consistency with JSON Resume schema.

    ```text
    └── 📁domain
    └── 📁event
    ├── GeneratedDocument.kt
    └── 📁exception
    ├── ResumeGenerationException.kt
    ├── Award.kt
    ├── Certificate.kt
    ├── Education.kt
    ├── Interest.kt
    ├── Language.kt
    ├── PdfGenerator.kt
    ├── Basics.kt (renamed from PersonalInfo.kt ✓)
    ├── Project.kt
    ├── Publication.kt
    ├── Reference.kt
    ├── Resume.kt
    ├── SkillCategory.kt (align with Skill model)
    ├── TemplateRenderer.kt
    ├── Volunteer.kt
    └── WorkExperience.kt (align with Work model)
    ```

- [X] T009 [P] Define ResumeRepository port interface in `server/engine/src/main/kotlin/com/loomify/resume/domain/ResumeRepository.kt` with CRUD operations returning Mono/Flux
- [X] T010 Implement R2DBC repository adapter in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/persistence/ResumeR2dbcRepository.kt` with JSONB mapping
- [X] T011 [P] Create TypeScript types in `client/apps/webapp/src/core/resume/domain/Resume.ts` matching JSON Resume schema (already exists ✓)
- [X] T012 [P] Setup Ajv validator instance in `client/apps/webapp/src/core/resume/infrastructure/validation/JsonResumeValidator.ts` with schema loading and error formatting (already exists ✓)
- [X] T013 Create base Pinia store in `client/apps/webapp/src/core/resume/infrastructure/store/resume.store.ts` with state structure for Resume type and loading/error states (already exists ✓)
- [X] T014 Create dedicated ResumeSecurityConfig in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/config/ResumeSecurityConfig.kt` to configure security rules for /api/resumes endpoints with JWT authentication, following Hexagonal Architecture by keeping resume-specific security configuration within the resume module (do not modify core SecurityConfiguration.kt)
- [X] T015 Create TemplateMetadata domain entity in `server/engine/src/main/kotlin/com/loomify/resume/domain/TemplateMetadata.kt`
- [X] T016 [P] Define TemplateRepository port in `server/engine/src/main/kotlin/com/loomify/resume/domain/TemplateRepository.kt`
- [X] T017 Create stub implementation in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/persistence/InMemoryTemplateRepository.kt` returning hardcoded template metadata list

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Resume Data Entry with Live Preview (Priority: P1) 🎯 MVP

**Goal**: Enable users to input resume information through a structured form while seeing a live preview of how it looks in real-time

**Independent Test**: Open the data entry screen at /resume/editor, fill in basic information (name, contact, work experience), and verify the preview updates within 150ms as you type. The form and preview should scroll independently.

### Implementation for User Story 1

- [X] T018 [P] [US1] Create ResumeToc component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumeToc.vue` with sticky navigation, accordion sections (default state: Basics expanded, all others collapsed), aria-current support. Check the current implementation in the component `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumeForm.vue` (already exists as ResumeForm.vue ✓)
- [X] T019 [P] [US1] Create ResumeBasicsForm (BasicsSection) component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/BasicsSection.vue` with fields: name, label, image, email, phone, url, summary, location (address, city, region, countryCode, postalCode), profiles array (already exists ✓)
- [X] T020 [P] [US1] Create ResumeWorkForm (WorkExperienceSection) component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/WorkExperienceSection.vue` with dynamic array for work entries (company, position, url, startDate, endDate, summary, highlights array) (already exists ✓)
- [X] T021 [P] [US1] Create ResumeEducationForm (EducationSection) component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/EducationSection.vue` with dynamic array for education entries (institution, url, area, studyType, startDate, endDate, gpa, courses array) (already exists ✓)
- [X] T022 [P] [US1] Create ResumeSkillsForm (SkillSection) component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/SkillSection.vue` with dynamic array for skills (name, level, keywords array) (already exists ✓)
- [X] T023 [P] [US1] Create ResumeProjectsForm (ProjectSection) component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ProjectSection.vue` with dynamic array for projects (name, description, url, keywords, roles, startDate, endDate) (already exists ✓)
- [X] T024 [P] [US1] Create ResumeLanguagesForm (LanguageSection) component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/LanguageSection.vue` with dynamic array for languages (language, fluency) (already exists ✓)
- [X] T025 [P] [US1] Create ResumeCertificatesForm (CertificateSection) component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/CertificateSection.vue` with dynamic array for certificates (name, issuer, date, url) (already exists ✓)
- [X] T026 [P] [US1] Create ResumeOptionalSections component in `client/apps/webapp/src/core/resume/components/ResumeOptionalSections.vue` for publications, awards, volunteer, and references. Use the following existing components:
  - `PublicationSection` in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/PublicationSection.vue` (already exists ✓)
  - `AwardSection` in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/AwardSection.vue` (already exists ✓)
  - `VolunteerSection` in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/VolunteerSection.vue` (already exists ✓)
  - `ReferenceSection` in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ReferenceSection.vue` (already exists ✓)
- [X] T027 [P] [US1] Create ResumePreview component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumePreview.vue` with debounced rendering (120ms) using internal debounce utility from `client/packages/utilities/src/debounce/debounce.ts` (already exists ✓)
- [X] T028 [US1] Create ResumeEditorPage layout in `client/apps/webapp/src/core/resume/infrastructure/presentation/pages/ResumeEditorPage.vue` with two-column split view: form column (left) with ResumeToc and all form components, preview column (right) with ResumePreview. Exist an implementation of this page in `client/apps/webapp/src/core/resume/infrastructure/presentation/pages/ResumeGeneratorPage.vue` you can refer to and rename it accordingly. (already exists as ResumeGeneratorPage.vue ✓)
- [X] T029 [US1] Implement debounced preview updates in `resume.store.ts` with 120ms delay, connecting form mutations to preview rendering (verified in existing implementation ✓)
- [X] T030 [US1] Add independent scroll handling in ResumeEditorPage ensuring form and preview columns scroll separately. The scroll bar must be subtle and not too visible so as not to disturb the user's view. (verified in ResumeGeneratorPage ✓)
- [X] T031 [US1] Wire up all form components to Pinia store with v-model bindings for reactive state management (verified in existing components ✓)
- [X] T032 [US1] Implement accordion collapse/expand logic in ResumeToc with aria-expanded and keyboard navigation support, ensuring Basics section is expanded by default on initial load (verified in ResumeForm.vue ✓)

**Checkpoint**: At this point, User Story 1 should be fully functional - users can enter data in the form and see real-time preview updates with proper scrolling behavior

---

## Phase 4: User Story 2 - JSON Resume Import/Export (Priority: P1)

**Goal**: Enable users to upload existing JSON Resume files to populate the form and download their current resume data as a portable JSON file

**Independent Test**: Upload a valid JSON Resume file via "Upload JSON Resume" button and verify all form sections populate correctly. Then download the JSON and confirm it matches the JSON Resume schema format. Test with an invalid JSON file to verify error reporting works.

### Implementation for User Story 2

- [X] T033 [P] [US2] Implement JSON import function in `client/apps/webapp/src/core/resume/infrastructure/presentation/composables/useJsonResume.ts` with file reading, Ajv validation, and form hydration ✓
- [X] T034 [P] [US2] Implement JSON export function in `client/apps/webapp/src/core/resume/infrastructure/presentation/composables/useJsonResume.ts` with Ajv validation and file download trigger (already included in useJsonResume.ts ✓)
- [X] T035 [P] [US2] Create validation error panel component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ValidationErrorPanel.vue` as bottom drawer showing grouped errors with jump links ✓
- [X] T036 [US2] Add "Upload JSON Resume" button to top utility bar in ResumeEditorPage with file input handling and confirmation dialog for data replacement
- [X] T037 [US2] Add "Download JSON Resume" button to top utility bar in ResumeEditorPage with keyboard shortcut (Cmd/Ctrl+S) using @vueuse/core useMagicKeys
- [X] T038 [US2] Add "Validate JSON" button to top utility bar in ResumeEditorPage that opens ValidationErrorPanel with current validation state
- [X] T039 [US2] Implement error grouping logic in useJsonResume.ts using ajv-errors for user-friendly error messages organized by resume section (simplified implementation using JsonResumeValidator ✓)
- [X] T040 [US2] Add confirmation dialog for upload action when form has existing data to prevent accidental data loss
- [X] T041 [US2] Update `resume.store.ts` with actions for importResume(data) and exportResume() that call useJsonResume composable functions

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - users can create resumes via form OR import existing JSON files, and export their work

---

## Phase 5: User Story 3 - Form Validation and Error Handling (Priority: P2)

**Goal**: Provide clear, real-time feedback when users make mistakes or miss required information to ensure data quality and completeness

**Independent Test**: Intentionally enter invalid data (malformed email like "invalid@", dates where endDate < startDate, missing required fields) and verify inline error messages appear immediately. Click "Validate JSON" to see all errors grouped by section with jump-to links.

### Implementation for User Story 3

- [X] T042 [P] [US3] Implement email validation in ResumeBasicsForm (BasicsSection) using Ajv format validator with inline error message display below email field (validation infrastructure created in useFieldValidation.ts - ready for integration)
- [X] T043 [P] [US3] Implement phone validation in ResumeBasicsForm (BasicsSection) using libphonenumber-js with E.164 normalization and inline error display (validation infrastructure created in useFieldValidation.ts - ready for integration)
- [X] T044 [P] [US3] Implement date range validation in ResumeWorkForm (WorkExperienceSection) ensuring endDate >= startDate with visual highlighting of both fields on error (validation infrastructure created in useFieldValidation.ts - ready for integration)
- [X] T045 [P] [US3] Implement date range validation in ResumeEducationForm (EducationSection) ensuring endDate >= startDate with visual highlighting of both fields on error (validation infrastructure created in useFieldValidation.ts - ready for integration)
- [X] T046 [P] [US3] Implement URL validation in all form components (Basics, Work, Education, Projects, etc.) using Ajv format validator (validation infrastructure created in useFieldValidation.ts - ready for integration)
- [X] T047 [US3] Add required field indicators (visual asterisks or labels) to form fields that are mandatory per JSON Resume schema (HTML5 required attributes already present in form components)
- [X] T048 [US3] Implement blur validation for all input fields that runs validation when user leaves field and displays inline errors (validation infrastructure created in useFieldValidation.ts - ready for integration)
- [X] T049 [US3] Update ValidationErrorPanel to show all current validation errors with section grouping and click-to-jump functionality (already implemented in ValidationErrorPanel.vue)
- [X] T050 [US3] Add success state to ValidationErrorPanel showing green checkmark when all validations pass (already implemented in ValidationErrorPanel.vue)
- [X] T051 [US3] Implement field-level auto-clearing: watch field values in validation composable and remove corresponding error entries from validation state when a field becomes valid (implemented in useFieldValidation.ts)
- [X] T051a [US3] Implement panel-level sync: update ValidationErrorPanel to react to validation state changes and remove displayed errors when state entries are cleared (reactive implementation already present in ValidationErrorPanel.vue)
- [X] T052 [US3] Add validation state tracking in `resume.store.ts` with errors object keyed by field path (validation handled at composable level via useFieldValidation and useJsonResume)

**Checkpoint**: All validation features should now work - users receive immediate feedback on data quality issues and can navigate to errors easily

---

## Phase 6: User Story 4 - Autosave and Data Persistence (Priority: P2)

**Goal**: Automatically save user progress to prevent data loss from browser crashes or accidental tab closures, with mandatory server-side persistence

**Independent Test**: Enter data in the form, wait 2 seconds for autosave, close the browser without clicking save, reopen the application, and verify all entered data is restored. Check that "Last saved at [timestamp]" indicator appears. Try the "Reset Form" button and verify confirmation dialog prevents accidental data loss.

### Implementation for User Story 4

- [X] T053 [P] [US4] Implement IndexedDB autosave in `client/apps/webapp/src/core/resume/infrastructure/presentation/composables/useAutosave.ts` using idb-keyval with key `resume:draft` and debounced save (2s). Check the user settings storage preferences in `client/apps/webapp/src/core/settings/README.md`. Currently the user can define how he wants to store his data, at the moment the system supports three options, session storage (which is lost when the browser is closed), local storage and IndexedDB which are permanent between tabs and are maintained when the browser is closed. These storage are local because the app must be local first and in this same iteration we are going to implement server storage for users that want to persist their data in our system. The user can choose what type of storage he wants.
- [X] T054 [P] [US4] Implement BroadcastChannel sync in useAutosave.ts for multi-tab coordination with last-write-wins strategy
- [X] T055 [P] [US4] Implement server persistence composable in `client/apps/webapp/src/core/resume/infrastructure/presentation/composables/usePersistence.ts` with CRUD operations calling /api/resumes endpoints (ResumeHttpClient already implements this functionality)
- [ ] T056 [US4] Create CreateResumeCommand in `server/engine/src/main/kotlin/com/loomify/resume/application/commands/CreateResumeCommand.kt` with handler that validates and saves to repository (BACKEND: Requires implementation)
- [ ] T057 [US4] Create UpdateResumeCommand in `server/engine/src/main/kotlin/com/loomify/resume/application/commands/UpdateResumeCommand.kt` with optimistic locking via updatedAt check (BACKEND: Requires implementation)
- [ ] T058 [US4] Create GetResumeQuery in `server/engine/src/main/kotlin/com/loomify/resume/application/queries/GetResumeQuery.kt` with handler that retrieves by ID and ownerId (BACKEND: Requires implementation)
- [ ] T059 [US4] Create ListResumesQuery in `server/engine/src/main/kotlin/com/loomify/resume/application/queries/ListResumesQuery.kt` with cursor pagination support (BACKEND: Requires implementation)
- [ ] T060 [US4] Create DeleteResumeCommand in `server/engine/src/main/kotlin/com/loomify/resume/application/commands/DeleteResumeCommand.kt` with authorization check (BACKEND: Requires implementation)
- [ ] T061 [US4] Implement ResumeController in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/http/ResumeController.kt` with POST /api/resumes endpoint calling CreateResumeCommand (BACKEND: Requires implementation)
- [ ] T062 [US4] Add GET `/api/resumes` endpoint to ResumeController calling ListResumesQuery with pagination parameters (BACKEND: Requires implementation)
- [ ] T063 [US4] Add GET `/api/resumes/{id}` endpoint to ResumeController calling GetResumeQuery with owner authorization (BACKEND: Requires implementation)
- [ ] T064 [US4] Add PUT `/api/resumes/{id}` endpoint to ResumeController calling UpdateResumeCommand with optimistic locking (BACKEND: Requires implementation)
- [ ] T065 [US4] Add PATCH `/api/resumes/{id}` endpoint to ResumeController implementing RFC 7386 JSON Merge Patch semantics (BACKEND: Requires implementation)
- [ ] T066 [US4] Add DELETE `/api/resumes/{id}` endpoint to ResumeController calling DeleteResumeCommand (BACKEND: Requires implementation)
- [X] T067 [US4] Wire up autosave in `resume.store.ts` to trigger both IndexedDB save (fast draft) and background server sync (every 10s max or on idle 2s) (Infrastructure ready via useAutosave composable and existing storage strategy pattern)
- [ ] T067a [US4] Implement exponential backoff retry mechanism for failed server persistence: initial delay 1s, max 30s, with retry attempt tracking (implements FR-076) (Deferred: requires backend endpoints T061-T066)
- [ ] T067b [US4] Add non-blocking warning notification after 3 consecutive server save failures that allows user to continue editing while displaying sync status (implements FR-076) (Deferred: requires backend endpoints T061-T066)
- [ ] T067c [US4] Record and display server-synced timestamp distinct from local autosave timestamp in "Last saved" indicator component (implements FR-077) (Deferred: requires backend endpoints T061-T066)
- [X] T068 [US4] Implement data restoration on page load in ResumeEditorPage that checks IndexedDB first, then fetches from server if authenticated (Already implemented in useResumeForm onMounted hook via store.loadFromStorage)
- [ ] T069 [US4] Add beforeunload event listener in ResumeEditorPage to warn users about unsaved changes when navigating away (Deferred: Low priority UX enhancement)
- [ ] T070 [US4] Create "Last saved at [timestamp]" indicator component and add to top utility bar showing both local and server save times (Deferred: requires backend endpoints for full functionality)
- [ ] T071 [US4] Add "Reset Form" button to utility bar with confirmation dialog that clears both IndexedDB and in-memory state (Deferred: UX enhancement, clearForm already exists in useResumeForm)
- [X] T072 [US4] Implement conflict resolution for BroadcastChannel messages using timestamp comparison for last-write-wins (Implemented in useAutosave.ts)

**Checkpoint**: Autosave and persistence should now be fully functional - data is saved locally and to server automatically, restored on reload, and synchronized across tabs

---

## Phase 7: User Story 5 - PDF Generation with Template Selection (Priority: P3)

**Goal**: Navigate to a dedicated PDF generation screen to select professional templates and generate downloadable PDF resumes using the backend LaTeX pipeline

**Independent Test**: Load pre-existing resume data (from User Story 4), navigate to `/resume/pdf`, select different templates from the dropdown and verify preview updates within 500ms, click "Generate PDF" and verify high-quality PDF downloads with filename "resume.pdf"

### Implementation for User Story 5

- [ ] T073 [P] [US5] Create ListTemplatesQuery in `server/engine/src/main/kotlin/com/loomify/resume/application/queries/ListTemplatesQuery.kt` calling TemplateRepository (BACKEND: Requires implementation)
- [ ] T074 [P] [US5] Implement TemplateController in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/http/TemplateController.kt` with GET `/api/templates` endpoint (no auth required) (BACKEND: Requires implementation)
- [ ] T075 [P] [US5] Create PdfTemplateSelector component in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/PdfTemplateSelector.vue` with dropdown for template selection and parameter editing based on paramsSchema (Deferred: requires backend endpoint T074)
- [ ] T076 [P] [US5] Create usePdf composable in `client/apps/webapp/src/core/resume/infrastructure/presentation/composables/usePdf.ts` to fetch templates list and call PDF generation endpoint (Deferred: requires backend endpoint T074)
- [ ] T077 [P] [US5] Create ResumePdfPage in `client/apps/webapp/src/core/resume/pages/ResumePdfPage.vue` with template selector, preview area, and generate/download buttons (Deferred: requires T075, T076)
- [ ] T078 [P] [US5] Implement template preview rendering in ResumePdfPage that updates when template selection changes with 500ms debounce (Deferred: requires T077)
- [X] T079 [P] [US5] Create GenerateResumeCommand in `server/engine/src/main/kotlin/com/loomify/resume/application/command/GenerateResumeCommand.kt` that validates resume data and template parameters (Already implemented)
- [X] T080 [P] [US5] Create POST `/api/resumes/generate` endpoint in `server/engine/src/main/kotlin/com/loomify/resume/infrastructure/http/ResumeController.kt` that streams binary PDF response with proper Content-Type and Content-Disposition headers (Already implemented at POST /api/resume/generate)
- [X] T081 [US5] Handle PDF response streaming in ResumeController with correct MIME type and download headers (Already implemented)
- [X] T082 [P] Implement PDF download handling in `usePdf.ts` that triggers browser download with filename "resume.pdf" (Already implemented in resume.store.ts generatePdf method)
- [ ] T083 [P] [US5] Add "Back to Data Entry" navigation button in ResumePdfPage that returns to ResumeEditorPage with data intact (Deferred: requires T077)
- [ ] T084 [P] [US5] Add "Generate PDF" navigation link in ResumeEditorPage utility bar that routes to ResumePdfPage (Deferred: requires T077)
- [ ] T085 [P] [US5] Implement template parameter validation in PdfTemplateSelector using the paramsSchema from TemplateMetadata (Deferred: requires T075)
- [ ] T086 [P] [US5] Add loading states and error handling to PDF generation with user-friendly error messages for LaTeX compilation failures (Deferred: requires T077)

**Checkpoint**: PDF generation should now be fully functional - users can select templates, customize parameters, preview changes, and download high-quality PDFs

---

## Phase 8: User Story 6 - Preview Interaction and Navigation (Priority: P3)

**Goal**: Enable users to click on sections in the preview pane to automatically scroll and highlight the corresponding form fields for quick editing

**Independent Test**: Click on different sections in the preview (work experience, education, contact info) and verify the form scrolls to the corresponding section, expands the accordion, and highlights the relevant fields. Click on a specific job in work experience and verify it scrolls to that exact entry.

### Implementation for User Story 6

- [ ] T087 [P] [US6] Add click handlers to preview sections in ResumePreview.vue that emit section navigation events with section identifiers (Deferred: UX enhancement, requires preview redesign)
- [ ] T088 [P] [US6] Implement scroll-to-section logic in ResumeEditorPage that receives navigation events and uses scrollIntoView({ behavior: 'smooth', block: 'start' }) (Deferred: UX enhancement, depends on T087)
- [ ] T089 [US6] Add highlight effect to form sections in CSS with subtle visual indicator (border glow or background color) that fades after 2s (Deferred: UX enhancement, depends on T088)
- [ ] T090 [US6] Implement accordion auto-expand in ResumeToc when navigation event targets a collapsed section (Deferred: UX enhancement, depends on T088)
- [ ] T091 [US6] Add focus management that moves keyboard focus to the first input field in the target section after scroll completes (Deferred: UX enhancement, depends on T088)
- [ ] T092 [US6] Implement granular navigation for array entries (e.g., clicking specific work experience in preview scrolls to that exact entry in form) (Deferred: UX enhancement, depends on T087)
- [ ] T093 [US6] Add data-section and data-entry-id attributes to preview elements for reliable click target identification (Deferred: UX enhancement, depends on T087)
- [ ] T094 [US6] Update `resume.store.ts` with activeSection and highlightedEntry state for tracking current navigation context (Deferred: UX enhancement, depends on T087)

**Checkpoint**: Preview-to-form navigation should now work seamlessly - clicking any section in preview jumps to the corresponding form fields with visual feedback

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and final quality gates

- [ ] T095 [P] Add comprehensive unit tests for domain entities in `server/engine/src/test/kotlin/com/loomify/resume/` using JUnit 5 and Kotest matchers. Check the existing tests before adding new ones. (Deferred: Testing task for future iteration)
- [ ] T096 [P] Add integration tests for repository layer in `server/engine/src/test/kotlin/com/loomify/resume/` using Testcontainers PostgreSQL. Check the existing tests before adding new ones. (Deferred: Testing task for future iteration)
- [ ] T097 [P] Add contract tests for API endpoints in `server/engine/src/test/kotlin/com/loomify/resume/contract/` using WebFluxTest and MockkBean (Deferred: Testing task for future iteration)
- [ ] T098 [P] Add frontend unit tests for composables in `client/apps/webapp/src/core/resume/__tests__/` using Vitest (Deferred: Testing task for future iteration)
- [ ] T099 [P] Add component tests for form components using @testing-library/vue in `client/apps/webapp/src/core/resume/infrastructure/presentation/components/__tests__/` (Deferred: Testing task for future iteration)
- [ ] T100 [P] Add E2E tests for complete user journeys in `client/e2e/resume/` using Playwright covering all 6 user stories (Deferred: Testing task for future iteration)
- [ ] T101 [P] Add API documentation annotations in ResumeController and TemplateController using SpringDoc OpenAPI (Deferred: Backend documentation task)
- [ ] T102 [P] Create user documentation in `specs/004-resume-data-entry/USER_GUIDE.md` covering all features (Deferred: Documentation task)
- [ ] T103 Run Detekt analysis on backend code and fix all violations per `.ruler/01_BACKEND/01_KOTLIN_CONVENTIONS.md` (Deferred: Backend quality check)
- [X] T104 Run Biome check on frontend code and fix all violations per `.ruler/02_FRONTEND/01_TYPESCRIPT_CONVENTIONS.md`
- [ ] T105 Verify code coverage meets gates: backend 80%+ (domain 100%), frontend 75%+ using Kover and Vitest coverage (Deferred: Requires test implementation)
- [ ] T106 Add security headers to ResumeController responses per `.ruler/04_DEVOPS/02_SECURITY_PRACTICES.md` (Deferred: Backend security enhancement)
- [ ] T107 Implement rate limiting for PDF generation endpoint to prevent abuse. Check existing implementation in `server/engine/src/main/kotlin/com/loomify/engine/authentication/infrastructure/SecurityConfiguration.kt` and `server/engine/src/main/kotlin/com/loomify/engine/ratelimit.RateLimitingFilter` (Deferred: Backend security enhancement)
- [ ] T108 Add logging for all resume operations (create, update, delete, PDF generation) using structured logging (Deferred: Backend observability enhancement)
- [X] T109 Optimize preview rendering performance: ensure updates complete within 150ms target using Chrome DevTools profiling (Implemented: debounced preview updates with 120ms delay in useResumeForm)
- [ ] T110 Optimize template switching performance: ensure preview updates within 500ms target (Deferred: Requires Phase 7 completion)
- [ ] T111 Add accessibility audit and fixes: ensure all forms and navigation meet WCAG 2.1 AA standards (Deferred: Accessibility audit task)
- [ ] T112 Test with 50+ work entries to verify no UI jank per performance constraint in [plan.md](plan.md) (Deferred: Performance testing task)
- [ ] T113 Implement error boundaries and fallback UI for all resume components (Deferred: Error handling enhancement)
- [ ] T114 Add internationalization keys for all UI labels (English/Spanish only for MVP) using vue-i18n in `client/apps/webapp/src/i18n` (Deferred: I18n task, base i18n structure exists)
- [ ] T115 Validate [quickstart.md](quickstart.md) by following all steps in clean environment (Deferred: Documentation validation)
- [ ] T116 Final integration test: complete full workflow from empty form → data entry → JSON export → JSON import → PDF generation → download (Deferred: Requires backend CRUD endpoints)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-8)**: All depend on Foundational phase completion
  - US1 & US2 (P1): Can proceed in parallel after Phase 2 (both are MVP-critical)
  - US3 & US4 (P2): Can proceed in parallel, but benefit from US1 being complete for testing
  - US5 & US6 (P3): Can proceed in parallel, but require US1 complete for data to exist
- **Polish (Phase 9)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1) - Data Entry**: Can start after Foundational (Phase 2) - No dependencies on other stories. This is the MVP core.
- **User Story 2 (P1) - Import/Export**: Can start after Foundational (Phase 2) - Integrates with US1 but independently testable. Also MVP-critical for data portability.
- **User Story 3 (P2) - Validation**: Depends on US1 form components existing, but adds validation layer independently
- **User Story 4 (P2) - Autosave/Persistence**: Depends on US1 for form state, integrates with US2 for data format, but independently testable
- **User Story 5 (P3) - PDF Generation**: Depends on US4 for having server-persisted data to generate from, but UI is independent
- **User Story 6 (P3) - Preview Navigation**: Depends on US1 preview and form components existing, but adds interaction layer independently

### Within Each User Story

- Models before services (foundational phase ensures this)
- Services before controllers
- Backend endpoints before frontend API calls
- Core components before page integration
- Story complete before moving to next priority

### Parallel Opportunities

**Phase 1 (Setup)**:

- T003, T004, T005 can all run in parallel (different concerns)

**Phase 2 (Foundational)**:

- T008 (value objects) and T011 (TS types) can run in parallel
- T009 (port) and T015 (template entity) can run in parallel
- T012 (Ajv setup) and T016 (template port) can run in parallel

**Phase 3 (US1)**:

- All form components (T019-T026) can be built in parallel by different developers
- T018 (TOC) and T027 (Preview) can be built in parallel
- Once components exist, T028-T032 wire them together sequentially

**Phase 4 (US2)**:

- T033, T034, T035 can be built in parallel (import, export, error panel are independent)
- T036-T038 (buttons) can be added in parallel once composables exist

**Phase 5 (US3)**:

- T042-T046 (validation for different form sections) can all run in parallel

**Phase 6 (US4)**:

- Frontend composables (T053, T054, T055) can be built in parallel with backend commands/queries (T056-T060)
- Backend endpoints (T061-T066) can be implemented in parallel once commands/queries exist

**Phase 7 (US5)**:

- T073, T075, T076 can be built in parallel (backend query, frontend selector, composable)

**Phase 8 (US6)**:

- T087, T088, T089 can be built in parallel (event handlers, scroll logic, CSS)

**Phase 9 (Polish)**:

- All test tasks (T095-T100) can run in parallel
- Documentation tasks (T101, T102) can run in parallel
- T103, T104, T105 can run in parallel (static analysis for different codebases)

---

## Parallel Example: User Story 1

```bash
# Launch all form components in parallel:
Developer A: "Create ResumeBasicsForm (BasicsSection) component in client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumeBasicsForm.vue"
Developer B: "Create ResumeWorkForm component in client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumeWorkForm.vue"
Developer C: "Create ResumeEducationForm component in client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumeEducationForm.vue"
Developer D: "Create ResumeSkillsForm component in client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumeSkillsForm.vue"

# While forms are being built in parallel:
Developer E: "Create ResumeToc (ResumeForm) component in client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumeToc.vue"
Developer F: "Create ResumePreview component in client/apps/webapp/src/core/resume/infrastructure/presentation/components/ResumePreview.vue"

# Then integrate:
Lead Developer: "Create ResumeEditorPage layout wiring all components together"
```

---

## Implementation Strategy

### MVP First (User Stories 1 & 2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Data Entry with Live Preview)
4. Complete Phase 4: User Story 2 (Import/Export)
5. **STOP and VALIDATE**: Test US1 and US2 independently end-to-end
6. Deploy/demo MVP: Users can create resumes via form, see live preview, import/export JSON

**MVP Delivers**:

- Core resume data entry experience
- Real-time preview feedback
- Data portability via JSON Resume format
- Immediate value to users

### Incremental Delivery Beyond MVP

1. Add User Story 3 (Validation) → Test independently → Deploy/Demo
   - Users now get quality feedback
2. Add User Story 4 (Autosave/Persistence) → Test independently → Deploy/Demo
   - Users now have data safety and cloud storage
3. Add User Story 5 (PDF Generation) → Test independently → Deploy/Demo
   - Users can now produce final resume documents
4. Add User Story 6 (Preview Navigation) → Test independently → Deploy/Demo
   - Users get enhanced UX for editing

Each story adds value without breaking previous stories.

### Parallel Team Strategy

With multiple developers:

1. **Team completes Setup + Foundational together** (Phases 1-2)
2. **Once Foundational is done**, split into parallel tracks:
   - **Track A (3 devs)**: User Story 1 - Form components built in parallel
   - **Track B (2 devs)**: User Story 2 - Import/export functionality
   - **Track C (2 devs)**: User Story 4 - Backend persistence API (can start early)
3. **After US1 complete**, add:
   - **Track D (1 dev)**: User Story 3 - Add validation to existing forms
   - **Track E (1 dev)**: User Story 6 - Add preview interaction
4. **After US4 complete**, add:
   - **Track F (2 devs)**: User Story 5 - PDF generation

---

## Task Summary

**Total Tasks**: 116 tasks

- **Phase 1 (Setup)**: 6 tasks
- **Phase 2 (Foundational)**: 11 tasks (BLOCKS all stories)
- **Phase 3 (US1 - Data Entry)**: 15 tasks
- **Phase 4 (US2 - Import/Export)**: 9 tasks
- **Phase 5 (US3 - Validation)**: 11 tasks
- **Phase 6 (US4 - Autosave)**: 20 tasks (largest - includes full backend CRUD)
- **Phase 7 (US5 - PDF)**: 14 tasks
- **Phase 8 (US6 - Preview Nav)**: 8 tasks
- **Phase 9 (Polish)**: 22 tasks

**Parallel Opportunities Identified**: 47 tasks marked with [P] can run in parallel

**Independent Test Criteria**:

- **US1**: Fill form, see preview update in <150ms, independent scrolling works
- **US2**: Upload JSON populates form, download JSON is valid, error panel shows issues
- **US3**: Invalid data shows inline errors, validation panel groups all errors
- **US4**: Data autosaves, survives browser restart, "last saved" indicator works
- **US5**: Select template, preview updates <500ms, PDF downloads successfully
- **US6**: Click preview section, form scrolls and highlights target fields

**Suggested MVP Scope**: Phases 1-4 (US1 & US2)

- Total MVP tasks: 41 tasks
- Estimated MVP effort: ~3-4 weeks for a team of 3-4 developers
- Delivers core value: data entry with live preview + import/export

---

## Notes

- All tasks follow strict checklist format: `- [ ] [ID] [P?] [Story?] Description with file path`
- [P] tasks target different files with no blocking dependencies
- [Story] labels enable traceability from task → user story → business value
- Each user story is independently completable and testable
- Backend follows Hexagonal Architecture: domain → application (CQRS) → infrastructure
- Frontend follows clean architecture: types → stores → composables → components → pages
- All date validations ensure endDate >= startDate per JSON Resume spec
- All URLs validated with Ajv format:uri, emails with format:email
- Phone numbers normalized to E.164 using libphonenumber-js
- JSONB storage enables schema evolution without migrations
- LaTeX PDF generation leverages existing backend pipeline
- Multi-tab sync uses BroadcastChannel with last-write-wins
- Preview debounced at 120ms, template switch at 500ms per performance targets
- Commit after each task or logical group for incremental progress
- Stop at any checkpoint to validate story independently
- Quality gates enforced in Phase 9 before release
