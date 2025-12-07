# Tasks: PDF Section Selector

**Input**: Design documents from `/specs/005-pdf-section-selector/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md,
data-model.md, contracts/
**Tests**: Unit, component, and E2E tests included per Constitution II Testing Standards.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Maps task to a user story (US1, US2, US3, US4)
- Include exact file paths in descriptions

---

## Phase 0: Prerequisite Validation (GATE)

**Purpose**: Verify all required design artifacts exist and are signed off before implementation
begins.
**Blocker**: Phase 1 MUST NOT start until all required artifacts pass validation.

### Required Artifacts (must exist and be finalized)

| Artifact | Location                                 | Sign-off By               | Status     |
|----------|------------------------------------------|---------------------------|------------|
| spec.md  | `specs/005-pdf-section-selector/spec.md` | Product Owner / Tech Lead | ☑ Verified |
| plan.md  | `specs/005-pdf-section-selector/plan.md` | Tech Lead                 | ☑ Verified |

### Optional Artifacts (must exist OR be explicitly marked deferred)

| Artifact      | Location                                       | Status                 |
|---------------|------------------------------------------------|------------------------|
| research.md   | `specs/005-pdf-section-selector/research.md`   | ☑ Present / ☐ Deferred |
| data-model.md | `specs/005-pdf-section-selector/data-model.md` | ☑ Present / ☐ Deferred |
| contracts/    | `specs/005-pdf-section-selector/contracts/`    | ☑ Present / ☐ Deferred |

### Validation Task

- [x] T000 **Prerequisite Validation** - Validate design artifacts before implementation:
    1. Verify `spec.md` exists and contains Status: Draft → **Approved** or **Ready for
       Implementation**
    2. Verify `plan.md` exists and Constitution Check shows **PASS**
    3. Verify `research.md` exists (Complete status) OR document reason for deferral
    4. Verify `data-model.md` exists with all entity definitions OR document reason for deferral
    5. Verify `contracts/` directory contains `api-contracts.md` and `component-contracts.md` OR
       document reason for deferral
    6. Flag any missing/incomplete items and **block Phase 1** until resolved
    7. Record sign-off in this file by checking boxes above

**Checkpoint**: All required artifacts verified. Phase 1 may proceed.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Ensure working environment and dependencies are ready.

- [x] T001 Verify dependency install with `pnpm install` in `cvix`
- [x] T002 Confirm feature branch `005-pdf-section-selector` is checked out and synced in `cvix`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain and services needed by all user stories.

- [x] T003 Create `SectionVisibility` domain types per data-model in
  `client/apps/webapp/src/core/resume/domain/SectionVisibility.ts`
- [x] T004 [P] Add `createDefaultVisibility(resume)` factory aligning with resume shape in
  `client/apps/webapp/src/core/resume/domain/SectionVisibility.ts`
- [x] T005 [P] Implement persisted schema `SectionVisibilityStorageData` with TTL/versioning in
  `client/apps/webapp/src/core/resume/infrastructure/storage/SectionVisibilityStorage.ts`
- [x] T006 Implement `filterResume(resume, visibility)` service handling section order and item
  filters in `client/apps/webapp/src/core/resume/application/ResumeSectionFilterService.ts`

**Checkpoint**: Foundation ready - user story implementation can start.

---

## Phase 3: User Story 1 - Toggle Resume Sections for PDF Export (Priority: P1) 🎯 MVP

**Goal**: Users can enable/disable whole sections, see live preview updates, and persist
section-level choices.
**Independent Test**: Toggle section pills and download PDF; only selected sections appear,
preferences persist after refresh.

- [x] T007 [P] [US1] Create Pinia store `section-visibility.store.ts` with state/refs for section
  visibility in
  `client/apps/webapp/src/core/resume/infrastructure/store/section-visibility.store.ts`
- [x] T008 [P] [US1] Integrate storage load/save (SectionVisibilityStorageData) with 30-day TTL in
  `section-visibility.store.ts`
- [x] T009 [P] [US1] Implement `SectionTogglePanel.vue` container wiring section-level toggle
  handlers in
  `client/apps/webapp/src/core/resume/infrastructure/presentation/components/SectionTogglePanel.vue`
- [x] T010 [US1] Update `ResumePdfPage.vue` to mount SectionTogglePanel above preview and apply
  `filterResume` output to preview/PDF submission in
  `client/apps/webapp/src/core/resume/infrastructure/presentation/pages/ResumePdfPage.vue`
- [x] T011 [US1] Enforce Personal Details always enabled and no-data sections disabled/tooltip in
  `section-visibility.store.ts` and `SectionTogglePanel.vue`
- [x] T012 [US1] Ensure persistence key format `cvix-section-visibility-{resumeId}` and
  savedAt/version fields are written/read in `SectionVisibilityStorage.ts`

**Checkpoint**: Section-level toggling works with persistence and preview/PDF alignment.

---

## Phase 4: User Story 2 - Visual Feedback for Section State (Priority: P1)

**Goal**: Clear visual states for enabled/disabled sections per design tokens.
**Independent Test**: Visual inspection of pills shows correct active/inactive/hover/focus states
and a11y roles.

- [x] T013 [P] [US2] Build `SectionTogglePill.vue` with primary/outline variants, checkmark icon,
  hover/focus styles in
  `client/apps/webapp/src/core/resume/infrastructure/presentation/components/SectionTogglePill.vue`
- [X] T014 [P] [US2] Wire `SectionTogglePanel.vue` to render pills with correct props (enabled,
  hasData, expanded) and implement full accessibility audit in
  `client/apps/webapp/src/core/resume/infrastructure/presentation/components/SectionTogglePanel.vue`:
    - Test keyboard navigation (tab/arrow handling, focus order, focus ring)
    - Verify focus management (roving focus/aria-activedescendant if used)
    - Ensure correct ARIA roles, labels, and states for screen readers
    - Check color contrast for all pill states (enabled, disabled, hover, focus)
    - Run automated accessibility tools (axe, pa11y)
    - Perform manual screen reader walkthrough (VoiceOver/NVDA/JAWS)
    - Document all findings and required remediations
- [X] T015 [US2] Add responsive wrapping and spacing for pills (768px–2560px) using design tokens in
  `SectionTogglePanel.vue`

**Checkpoint**: Visual states and accessibility confirmed for section pills.

---

## Phase 5: User Story 3 - Select Individual Items Within Sections (Priority: P1)

**Goal**: Users can expand sections, toggle individual items/fields, and see filtered preview/PDF.
**Independent Test**: Expand a section, toggle items/Personal Details fields, confirm preview/PDF
reflect selections; auto-disable when all items off.

- [x] T016 [P] [US3] Implement `ItemToggleList.vue` rendering item toggles with labels/sublabels in
  `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ItemToggleList.vue`
- [x] T017 [P] [US3] Extend `section-visibility.store.ts` with item toggle actions, personalDetails
  field toggles, and auto-disable/notification when all items off
- [x] T018 [US3] Integrate inline expand/collapse (Collapsible) and item list rendering per section
  in `SectionTogglePanel.vue`
- [x] T019 [US3] Ensure `filterResume` handles item-level filters and Personal Details fields before
  PDF submit in `ResumeSectionFilterService.ts`

**Checkpoint**: Item-level toggling works with persistence and preview/PDF alignment.

---

## Phase 6: User Story 4 - Section Order Preservation (Priority: P2)

**Goal**: Preserve standard section order matching backend template; no reordering UI.
**Independent Test**: Generate PDF after various toggles; sections appear in fixed order per
`engineering.stg`.

- [x] T020 [US4] Lock `SECTION_TYPES` ordering to match backend template and annotate linkage to
  `server/engine/src/main/resources/templates/resume/engineering/engineering.stg` in
  `SectionVisibility.ts`
- [x] T021 [US4] Keep rendering/filtering logic using `SECTION_TYPES` sequence and prevent drag/drop
  or reorder affordances in `SectionTogglePanel.vue` and `ResumeSectionFilterService.ts`

**Checkpoint**: Section ordering remains fixed and aligned with backend template.

---

## Phase 7: Testing (Constitution Mandated)

**Purpose**: Fulfill Constitution II Testing Standards - unit, integration, and E2E coverage.

- [x] T025 [P] Write unit tests for `createDefaultVisibility` and filter functions in
  `client/apps/webapp/src/core/resume/domain/SectionVisibility.spec.ts`
- [x] T026 [P] Write unit tests for `filterResume` service covering all section types and edge cases
  in `client/apps/webapp/src/core/resume/application/ResumeSectionFilterService.spec.ts`
- [x] T027 [P] Write unit tests for store actions (toggle, expand, persist) in
  `client/apps/webapp/src/core/resume/infrastructure/store/section-visibility.store.spec.ts`
- [ ] T028 [P] Write component tests for SectionTogglePill (enabled/disabled/hover states, a11y)
  using @testing-library/vue - ⚠️ Test file created but requires cleanup setup between tests
- [ ] T029 [P] Write component tests for SectionTogglePanel (renders pills, emits events) using
  @testing-library/vue - ⚠️ Deferred: Requires mount setup with Pinia store and i18n
- [ ] T030 [P] Write component tests for ItemToggleList (item toggles, field toggles for Personal
  Details) - ⚠️ Deferred: Requires mount setup similar to T029
- [ ] T031 Write E2E test: toggle section visibility, verify live preview updates in
  `client/e2e/resume-pdf-section-selector.spec.ts`
- [ ] T032 Write E2E test: toggle sections/items, download PDF, verify content matches selections
- [ ] T033 Write E2E test: toggle preferences, refresh page, verify persistence
- [ ] T034 Write E2E test: verify responsive behavior at 768px, 1024px, 1440px breakpoints (toggle
  all sections and items at each breakpoint, confirm layout stability and pill wrapping per T015; do
  not limit to smoke test)

**Checkpoint**: All Constitution II testing requirements met.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Finalize shared resources and validation.

- [x] T022 [P] Add required i18n keys for sections, fields, and toasts in
  `client/apps/webapp/src/i18n/` locale files
- [x] T023 Run lint/typecheck in `cvix/client/apps/webapp` (`pnpm check` and
  `pnpm --filter webapp typecheck`) - ✅ Linting warnings fixed
- [x] T024 Update `quickstart.md` with any deviations (if any) and confirm localStorage TTL/version
  notes in `cvix/specs/005-pdf-section-selector/quickstart.md`

---

## Dependencies & Execution Order

- **Setup (Phase 1)** → **Foundational (Phase 2)** → User Stories (Phases 3–6) → **Polish**
- User stories are independent after Phase 2; execute in priority order (US1, US2, US3 are P1; US4
  is P2) or parallel if staffed.
- Within each story: store/services before UI wiring; persistence before preview/PDF integration.
- **Testing (Phase 7)**: Depends on Phases 3-6 completion; can run in parallel with Polish.

### User Story Dependencies

- **US1**: Depends on Phase 2; no other story dependencies.
- **US2**: Depends on Phase 2; can run parallel with US1 UI once store is stubbed.
- **US3**: Depends on Phase 2; builds on US1 store/filter; can start after store scaffolding exists.
- **US4**: Depends on Phase 2; light touches on domain/panel/filter to ensure order.

### Parallel Opportunities

- Phase 2: T004 and T005 can run in parallel; T006 can start after T003.
- US1: T007/T008 (store + persistence) can run parallel; T009 can proceed once store signatures are
  known.
- US2: T013/T014 parallel (component + wiring), then T015.
- US3: T016/T017 parallel (component + store), then T018/T019.
- Polish: T022/T023 parallel; T024 last if docs need updates.

---

## Implementation Strategy

- **MVP first**: Complete Phases 1–3 to deliver section-level toggling with persistence and PDF
  alignment.
- **Incremental**: Add US2 visual clarity, then US3 item-level control, then US4 ordering
  confirmation.
- **Validate after each story**: Ensure preview/PDF match visibility, persistence works, and
  accessibility remains intact.
