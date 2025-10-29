# Tasks: Workspace Selection Implementation

**Input**: Design documents from `/specs/002-workspace-selection/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: TDD approach explicitly requested - tests written FIRST before implementation

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Based on plan.md, all frontend code lives in:

- `client/apps/webapp/src/workspace/` - Feature root
- `client/e2e/` - E2E tests

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and workspace feature structure

- [X] T001 Create workspace feature directory structure per plan.md at `client/apps/webapp/src/workspace/`
- [X] T002 [P] Create domain directory with index.ts barrel export at `client/apps/webapp/src/workspace/domain/index.ts`
- [X] T003 [P] Create application directory with index.ts barrel export at `client/apps/webapp/src/workspace/application/index.ts`
- [X] T004 [P] Create infrastructure directory structure (store/, api/, storage/, http/) at `client/apps/webapp/src/workspace/infrastructure/`
- [X] T005 [P] Create presentation directory structure (components/, composables/) at `client/apps/webapp/src/workspace/presentation/`
- [X] T006 [P] Setup i18n keys for workspace feature in `client/apps/webapp/src/i18n/locales/en/workspace.json`
- [X] T007 [P] Setup i18n keys for workspace feature in `client/apps/webapp/src/i18n/locales/es/workspace.json`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core types, utilities, and infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Foundation Tests (TDD - Write First) 🧪

- [X] T008 [P] Write tests for WorkspaceId value object in `client/apps/webapp/src/workspace/domain/__tests__/WorkspaceId.test.ts`
- [X] T009 [P] Write tests for WorkspaceName value object in `client/apps/webapp/src/workspace/domain/__tests__/WorkspaceName.test.ts`
- [X] T010 [P] Write tests for Workspace entity factory in `client/apps/webapp/src/workspace/domain/__tests__/WorkspaceEntity.test.ts`
- [X] T011 Write tests for workspaceHttpClient extending BaseHttpClient in `client/apps/webapp/src/workspace/infrastructure/http/__tests__/workspaceHttpClient.test.ts`
- [X] T012 Write tests for workspaceLocalStorage adapter in `client/apps/webapp/src/workspace/infrastructure/storage/__tests__/workspaceLocalStorage.test.ts`

### Foundation Implementation 🔴 → ♻️ → ✅

- [X] T013 [P] Implement WorkspaceId value object with UUID validation in `client/apps/webapp/src/workspace/domain/WorkspaceId.ts`
- [X] T014 [P] Implement WorkspaceName value object with 1-100 char validation in `client/apps/webapp/src/workspace/domain/WorkspaceName.ts`
- [X] T015 [P] Define Workspace entity TypeScript interface in `client/apps/webapp/src/workspace/domain/WorkspaceEntity.ts`
- [X] T016 [P] Define WorkspaceError interface and WorkspaceErrorCode enum in `client/apps/webapp/src/workspace/domain/WorkspaceError.ts`
- [X] T017 Create workspaceHttpClient extending BaseHttpClient from `client/apps/webapp/src/shared/BaseHttpClient.ts` in `client/apps/webapp/src/workspace/infrastructure/http/workspaceHttpClient.ts`
- [X] T018 Implement local storage adapter with STORAGE_KEY constant in `client/apps/webapp/src/workspace/infrastructure/storage/workspaceLocalStorage.ts`
- [X] T019 Define WorkspaceApiClient interface in `client/apps/webapp/src/workspace/infrastructure/api/WorkspaceApiClient.ts`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Automatic Workspace Load on Login (Priority: P1) 🎯 MVP

**Goal**: Automatically load user's last selected or default workspace on successful login

**Independent Test**: Log in as a user with a previously selected workspace → workspace loads automatically within 2 seconds

### Tests for User Story 1 (TDD - Write First) 🧪

### Tests (Write First - TDD)

- [X] **T020** - Write WorkspaceSelectionService.determineWorkspaceToLoad() test
- [X] **T021** - Write workspaceApiClient.getAllWorkspaces() test
- [X] **T022** - Write workspaceStore.loadWorkspaces() test
- [X] **T023** - Write useWorkspaceLoader composable test
- [X] **T024** - Write E2E test: "should auto-load last selected workspace on login"

### Implementation for User Story 1 🔴 → ♻️ → ✅

### Implementation (Make Tests Pass - TDD)

- [X] **T025** - Implement WorkspaceSelectionService.determineWorkspaceToLoad() domain logic
- [X] **T026** - Implement workspaceHttpClient singleton export
- [X] **T027-T029** - Create Pinia workspace store (state, actions, getters)
- [X] **T030** - Implement useWorkspaceLoader composable with retry logic and logging
- [X] **T031** - Create Vue Router navigation guard for workspace auto-loading
- [X] **T032** - Integrate guard with router/index.ts
- [X] **T033** - Add error handling with 3-attempt exponential backoff retry (1s, 2s, 4s delays)
- [X] **T034** - Add console logging for workspace events (dev mode only)

**✅ Checkpoint**: User Story 1 is COMPLETE → users auto-load workspace on login with retry logic and dev logging

---

## Phase 4: User Story 2 - Manual Workspace Selection (Priority: P2)

**Goal**: Allow users to view and manually switch between available workspaces during session

**Independent Test**: Open workspace selector → view workspaces → click different workspace → verify switch within 3 seconds

### Tests for User Story 2 (TDD - Write First) 🧪

- [X] T035 [P] [US2] Write tests for workspaceApiClient.getWorkspace(id) in `client/apps/webapp/src/workspace/infrastructure/api/__tests__/workspaceApiClient.test.ts`
- [X] T036 [P] [US2] Write tests for workspaceStore.selectWorkspace(id) action in `client/apps/webapp/src/workspace/infrastructure/store/__tests__/workspaceStore.test.ts`
- [X] T037 [P] [US2] Write tests for workspaceLocalStorage.saveLastSelected() in `client/apps/webapp/src/workspace/infrastructure/storage/__tests__/workspaceLocalStorage.test.ts`
- [X] T038 [P] [US2] Write tests for useWorkspaceSelection composable in `client/apps/webapp/src/workspace/application/__tests__/useWorkspaceSelection.test.ts`
- [X] T039 [P] [US2] Write component tests for WorkspaceSelector.vue in `client/apps/webapp/src/workspace/presentation/components/__tests__/WorkspaceSelector.test.ts`
- [X] T040 [P] [US2] Write component tests for WorkspaceSelectorItem.vue in `client/apps/webapp/src/workspace/presentation/components/__tests__/WorkspaceSelectorItem.test.ts`
- [X] T041 [US2] Write E2E test for manual workspace selection in `client/e2e/workspace-selection.spec.ts` (test: "should switch workspace on manual selection")

### Implementation for User Story 2 🔴 → ♻️ → ✅

- [X] T042 [US2] Implement workspaceApiClient.getWorkspace(id) in `client/apps/webapp/src/workspace/infrastructure/api/workspaceApiClient.ts`
- [X] T043 [US2] Implement workspaceLocalStorage.saveLastSelected(userId, workspaceId) in `client/apps/webapp/src/workspace/infrastructure/storage/workspaceLocalStorage.ts`
- [X] T044 [US2] Implement workspaceLocalStorage.getLastSelected(userId) in `client/apps/webapp/src/workspace/infrastructure/storage/workspaceLocalStorage.ts`
- [X] T045 [US2] Implement workspaceStore.selectWorkspace(id) action with persistence in `client/apps/webapp/src/workspace/infrastructure/store/workspaceStore.ts`
- [X] T046 [US2] Implement useWorkspaceSelection composable exposing selectWorkspace, currentWorkspace, workspaces in `client/apps/webapp/src/workspace/application/useWorkspaceSelection.ts`
- [X] T047 [P] [US2] Create WorkspaceSelectorItem.vue component with workspace name, description, isDefault badge in `client/apps/webapp/src/workspace/presentation/components/WorkspaceSelectorItem.vue`
- [X] T048 [US2] Create WorkspaceSelector.vue container component using Shadcn-Vue Select in `client/apps/webapp/src/workspace/presentation/components/WorkspaceSelector.vue`
- [X] T049 [US2] Implement workspace selection click handler in WorkspaceSelector.vue calling selectWorkspace()
- [X] T050 [US2] Add @workspace-selected event emitter in WorkspaceSelector.vue
- [X] T051 [US2] Add ARIA labels and keyboard navigation (role="combobox", aria-expanded) to WorkspaceSelector.vue
- [X] T052 [US2] Integrate WorkspaceSelector component into app header/navigation in `client/apps/webapp/src/layouts/AppLayout.vue`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently → users can auto-load AND manually switch workspaces

---

## Phase 5: User Story 3 - Workspace Loading Feedback (Priority: P3)

**Goal**: Provide clear visual feedback during workspace loading and switching operations

**Independent Test**: Trigger workspace load/switch → observe loading indicators → verify feedback appears for operations >500ms

### Tests for User Story 3 (TDD - Write First) 🧪

- [ ] T053 [P] [US3] Write component tests for WorkspaceLoadingState.vue in `client/apps/webapp/src/workspace/presentation/components/__tests__/WorkspaceLoadingState.test.ts`
- [ ] T054 [P] [US3] Write component tests for WorkspaceErrorState.vue in `client/apps/webapp/src/workspace/presentation/components/__tests__/WorkspaceErrorState.test.ts`
- [ ] T055 [P] [US3] Write tests for useWorkspaceSelectorUI composable in `client/apps/webapp/src/workspace/presentation/composables/__tests__/useWorkspaceSelectorUI.test.ts`
- [ ] T056 [US3] Write E2E test for loading states in `client/e2e/workspace-selection.spec.ts` (test: "should show loading indicator during workspace switch")
- [ ] T057 [US3] Write E2E test for error states in `client/e2e/workspace-selection.spec.ts` (test: "should display error message when workspace load fails")

### Implementation for User Story 3 🔴 → ♻️ → ✅

- [ ] T058 [P] [US3] Create WorkspaceLoadingState.vue with Shadcn-Vue Skeleton component in `client/apps/webapp/src/workspace/presentation/components/WorkspaceLoadingState.vue`
- [ ] T059 [P] [US3] Create WorkspaceErrorState.vue with error message + retry button in `client/apps/webapp/src/workspace/presentation/components/WorkspaceErrorState.vue`
- [ ] T060 [US3] Implement useWorkspaceSelectorUI composable managing loading/error UI state in `client/apps/webapp/src/workspace/presentation/composables/useWorkspaceSelectorUI.ts`
- [ ] T061 [US3] Add conditional rendering for loading state in WorkspaceSelector.vue (show WorkspaceLoadingState when isLoading)
- [ ] T062 [US3] Add conditional rendering for error state in WorkspaceSelector.vue (show WorkspaceErrorState when error exists)
- [ ] T063 [US3] Add conditional rendering for empty state in WorkspaceSelector.vue (show "No workspaces available" when workspaces.length === 0)
- [ ] T064 [US3] Implement toast notifications for workspace switch success/error using Shadcn-Vue Toast
- [ ] T065 [US3] Add loading spinner overlay during workspace switch (disable interactions with isSwitching state)
- [ ] T066 [US3] Add 500ms delay threshold before showing loading indicator (optimistic UI)
- [ ] T067 [US3] Implement retry button handler in WorkspaceErrorState.vue calling workspaceStore.loadWorkspaces()

**Checkpoint**: All user stories should now be independently functional → complete workspace selection experience with feedback

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and final quality assurance

- [ ] T068 [P] Add JSDoc comments to all domain services, value objects, and composables
- [ ] T069 [P] Add accessibility audit with axe-core for workspace components
- [ ] T070 [P] Add performance monitoring for workspace load/switch times (log to console in dev)
- [ ] T071 Validate 5-minute cache TTL logic in workspaceStore (check cacheTimestamp + cacheTTL)
- [ ] T072 Add debouncing (300ms) to workspace search/filter if selector has search input
- [ ] T073 Optimize bundle size - ensure workspace feature is <10KB gzipped (analyze with vite-plugin-bundle-analyzer)
- [ ] T074 [P] Run Biome linting and fix any issues: `pnpm run lint --fix`
- [ ] T075 [P] Run Vitest coverage and ensure 75% minimum: `pnpm test:coverage -- workspace`
- [ ] T076 [P] Run Playwright E2E tests: `pnpm test:e2e -- workspace-selection`
- [ ] T077 Validate quickstart.md instructions work (follow installation, usage, testing steps)
- [ ] T078 Update CHANGELOG.md with workspace selection feature entry
- [ ] T079 Create PR description using spec.md and tasks.md completion summary

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion - Can run independently
- **User Story 2 (Phase 4)**: Depends on Foundational completion - Can run independently (but often builds on US1)
- **User Story 3 (Phase 5)**: Depends on Foundational completion - Can run independently (enhances US1 & US2)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Independent - No dependencies on other stories (only Foundational)
- **User Story 2 (P2)**: Integrates with User Story 1 (uses store, composables) but independently testable
- **User Story 3 (P3)**: Enhances User Story 1 & 2 (adds loading/error UI) but independently testable

### Within Each User Story (TDD Workflow)

1. **Tests FIRST** 🧪 - Write all tests for the story (they will fail)
2. **Verify RED** 🔴 - Confirm tests fail as expected
3. **Implementation** ♻️ - Implement functionality to make tests pass
4. **Verify GREEN** ✅ - Confirm all tests pass
5. **Refactor** (if needed) - Clean up code while keeping tests green

### Parallel Opportunities

- **Phase 1 (Setup)**: All T001-T007 can run in parallel (different directories)
- **Phase 2 (Foundational Tests)**: T008-T012 can run in parallel (different files)
- **Phase 2 (Foundational Implementation)**: T013-T019 can run in parallel (different files)
- **Phase 3 (US1 Tests)**: T020-T024 can run in parallel (different files)
- **Phase 4 (US2 Tests)**: T035-T041 can run in parallel (different files)
- **Phase 4 (US2 Components)**: T047 can run in parallel with other tasks (independent file)
- **Phase 5 (US3 Tests)**: T053-T057 can run in parallel (different files)
- **Phase 5 (US3 Components)**: T058-T059 can run in parallel (different files)
- **Phase 6 (Polish)**: T068-T076, T078 can run in parallel (different concerns)

**Team Strategy**: Once Foundational phase completes, all 3 user stories can be worked on in parallel by different developers.

---

## Parallel Example: User Story 1 Tests

```bash
# Launch all tests for User Story 1 together (TDD - write first):
Task: "Write tests for WorkspaceSelectionService.determineWorkspaceToLoad()"
Task: "Write tests for workspaceApiClient.getAllWorkspaces()"
Task: "Write tests for workspaceStore.loadWorkspaces() action"
Task: "Write tests for useWorkspaceLoader composable"
Task: "Write E2E test for auto-load on login"
```

---

## Parallel Example: User Story 1 Implementation

```bash
# After tests are RED, launch implementation tasks together:
Task: "Implement WorkspaceSelectionService.determineWorkspaceToLoad()"
Task: "Implement workspaceApiClient.getAllWorkspaces()"
# Note: T027-T029 (store) must be sequential (same file)
# Note: T030 depends on T025-T029 completing (uses their outputs)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. ✅ Complete Phase 1: Setup (7 tasks)
2. ✅ Complete Phase 2: Foundational (12 tasks - CRITICAL)
3. ✅ Complete Phase 3: User Story 1 (15 tasks)
4. **STOP and VALIDATE**: Test User Story 1 independently
   - Run unit tests: `pnpm test -- workspace`
   - Run E2E test: `pnpm test:e2e -- "auto-load"`
   - Manual test: Log in and verify workspace auto-loads
5. **Deploy/Demo if ready** → MVP delivers immediate value!

### Incremental Delivery

1. ✅ Setup + Foundational → Foundation ready (19 tasks)
2. ✅ Add User Story 1 → Test independently → Deploy/Demo (15 tasks) **MVP!**
3. ✅ Add User Story 2 → Test independently → Deploy/Demo (18 tasks)
4. ✅ Add User Story 3 → Test independently → Deploy/Demo (15 tasks)
5. ✅ Polish → Final release (12 tasks)

Each story adds value without breaking previous stories.

### Parallel Team Strategy

With 3 developers after Foundational phase completes:

- **Developer A**: User Story 1 (Auto-load) - 15 tasks
- **Developer B**: User Story 2 (Manual selection) - 18 tasks
- **Developer C**: User Story 3 (Loading feedback) - 15 tasks

Stories complete and integrate independently. Merge in priority order: US1 → US2 → US3.

---

## Test Coverage Goals

Per constitution (Principle II: TDD):

- **Target**: 75% minimum code coverage (frontend)
- **Breakdown**:
  - Domain layer: 100% (pure business logic)
  - Application layer (composables): 90%
  - Infrastructure layer: 80%
  - Presentation layer (components): 70%

**Validation**:

```bash
pnpm test:coverage -- workspace
# Should show ≥75% overall coverage
```

---

## Notes

- **[P]** tasks = different files, no dependencies within phase
- **[Story]** label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- **TDD workflow**: Tests FIRST 🧪 → Implementation 🔴 → Refactor ♻️ → Green ✅
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Run `pnpm check` before committing to ensure linting passes
