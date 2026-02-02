# Implementation Plan: Workspace Selection Implementation

**Branch**: `002-workspace-selection` | **Date**: 2025-10-28 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-workspace-selection/spec.md`

## Summary

Implement frontend workspace selection functionality that automatically loads the user's last selected or default workspace on login, allows manual workspace switching through an enhanced selector component, and provides loading/error feedback. The backend workspace API endpoints are already implemented and ready for integration. This feature will use Pinia for state management, local storage for persistence, and follow TDD principles with comprehensive test coverage.

## Technical Context

**Language/Version**: TypeScript 5.x (Frontend), Kotlin 2.0.20 (Backend - read-only integration)
**Primary Dependencies**: Vue.js 3.5.17+, Pinia 3.0.3+, Vite 7.0.4+, Tailwind CSS 4.1.11+, Shadcn-Vue (Reka UI)
**Storage**: Local Storage (last selected workspace), Backend API (workspace data)
**Testing**: Vitest (unit/integration), @testing-library/vue (component), Playwright (E2E)
**Target Platform**: Web browsers (Chrome, Firefox, Safari, Edge - latest 2 versions)
**Project Type**: Web application (Frontend only - backend API already exists)
**Performance Goals**:

- Workspace load on login: <2 seconds (per SC-001)
- Workspace switch: <3 seconds (per SC-002)
- Loading indicator threshold: 500ms (per SC-005)
- Frontend bundle impact: <10KB gzipped for workspace feature

**Constraints**:

- Must integrate with existing backend workspace endpoints without modifications
- Must work with existing authentication system (Keycloak OAuth2/OIDC)
- Must follow Hexagonal Architecture principles where applicable (domain logic separation)
- Must achieve 75% code coverage minimum (frontend)

**Scale/Scope**:

- Support users with up to 100 workspaces (per assumptions)
- Single workspace context per browser tab
- No offline mode required

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Hexagonal Architecture (Ports & Adapters) - ✅ PASS

**Status**: Compliant (Frontend adaptation)

- **Domain Logic**: Workspace selection business rules (default selection, fallback logic) will be in a domain service layer
- **Application Layer**: Composables will orchestrate domain logic and API calls (framework-agnostic logic)
- **Infrastructure Layer**: Pinia store (state adapter), API client (HTTP adapter), local storage adapter
- **Dependency Flow**: Composables depend on domain services, infrastructure adapters implement interfaces defined by composables

**Justification**: While Hexagonal Architecture is primarily a backend pattern, we adapt its principles:

- Business logic (workspace selection rules) separated from framework code
- Storage and API access abstracted behind interfaces
- Vue components only interact through composables, not directly with stores or API

### II. Test-Driven Development (TDD) - ✅ PASS

**Status**: Compliant

- **TDD Workflow**: Tests will be written first for all composables, stores, and components
- **Test Pyramid**:
  - Unit tests: Workspace domain logic, composables, utility functions (MockK/Vitest mocks)
  - Integration tests: Pinia store + API client integration, component + store integration
  - E2E tests: Login → auto-load, manual workspace selection, error handling flows
- **Coverage Targets**: 75% minimum (frontend requirement), targeting 85%+ for workspace logic
- **Test Organization**:
  - Component tests: Co-located with components (e.g., `WorkspaceSelector.vue` + `WorkspaceSelector.test.ts`)
  - Store tests: `src/workspace/infrastructure/store/__tests__/workspaceStore.test.ts`
  - E2E tests: `client/e2e/workspace-selection.spec.ts`

### III. Code Quality & Static Analysis - ✅ PASS

**Status**: Compliant

- **TypeScript Strict Mode**: Enabled in all workspace-related files
- **No `any` Usage**: All types explicitly defined, use `unknown` with type guards if needed
- **Biome Linting**: Zero violations required before merge
- **Function Size**: Max 50 lines per function
- **Naming Conventions**: `camelCase` for functions/variables, `PascalCase` for components/types
- **Documentation**: JSDoc comments for all public APIs and composables

### IV. Security-First Development - ✅ PASS

**Status**: Compliant

- **Access Control**: Backend enforces workspace access (trust backend authorization)
- **Input Validation**: Validate workspace IDs before API calls (UUID format)
- **XSS Prevention**: Use Vue's `{{ }}` templating (auto-escapes), no `v-html` usage
- **Secure Storage**: Local storage for non-sensitive data only (workspace ID, not tokens)
- **Error Handling**: Never expose backend error details to users, show user-friendly messages

### V. User Experience Consistency - ✅ PASS

**Status**: Compliant

- **Design System**: Use Shadcn-Vue components (Button, Select, Dialog, Toast)
- **Accessibility**:
  - Keyboard navigation for workspace selector (Tab, Enter, Escape, Arrow keys)
  - ARIA labels and roles for screen readers
  - Focus management during loading states
- **i18n**: All text through `vue-i18n` (`$t('workspace.selector.title')`)
- **Responsive**: Mobile-first design, test on 320px, 768px, 1024px, 1440px viewports
- **Loading States**: Skeleton loaders for workspace list, spinner for switching
- **Error States**: Toast notifications for errors with retry actions

### VI. Performance & Scalability - ✅ PASS

**Status**: Compliant

- **Performance Targets Met**:
  - <2s workspace load (backend <200ms + frontend render <500ms + buffer)
  - <3s workspace switch (API call + state update + UI re-render)
  - Loading indicators for operations >500ms
- **Optimization Strategies**:
  - Cache workspace list in memory (Pinia store) after first load
  - Lazy-load workspace-dependent components
  - Debounce rapid workspace switches (prevent race conditions)
  - Use `v-memo` for workspace list items (prevent unnecessary re-renders)

### VII. Observability & Monitoring - ✅ PASS

**Status**: Compliant

- **Logging**: Console logging in dev (suppressed in prod)
  - Log workspace selection events: `[WORKSPACE] Selected: {workspaceId}`
  - Log errors: `[WORKSPACE ERROR] Failed to load: {error message}`
- **Error Tracking**: Errors captured and displayed via toast notifications
- **User Actions**: Track workspace switches for analytics (if analytics system exists)

### Gate Evaluation Summary

**Result**: ✅ ALL GATES PASS

No constitution violations. No complexity tracking needed. Feature ready for Phase 0 research.

## Project Structure

### Documentation (this feature)

```text
specs/002-workspace-selection/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── workspace-api.yaml
├── checklists/
│   └── requirements.md  # Already created - validation checklist
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
client/apps/webapp/src/
├── workspace/
│   ├── domain/
│   │   ├── WorkspaceSelectionService.ts    # Business logic for workspace selection
│   │   ├── WorkspaceEntity.ts              # Workspace domain entity
│   │   └── __tests__/
│   │       ├── WorkspaceSelectionService.test.ts
│   │       └── WorkspaceEntity.test.ts
│   ├── application/
│   │   ├── useWorkspaceSelection.ts        # Main composable for workspace selection
│   │   ├── useWorkspaceLoader.ts           # Composable for loading workspaces
│   │   └── __tests__/
│   │       ├── useWorkspaceSelection.test.ts
│   │       └── useWorkspaceLoader.test.ts
│   ├── infrastructure/
│   │   ├── store/
│   │   │   ├── workspaceStore.ts           # Pinia store for workspace state
│   │   │   └── __tests__/
│   │   │       └── workspaceStore.test.ts
│   │   ├── api/
│   │   │   ├── workspaceApiClient.ts       # API client for workspace endpoints
│   │   │   └── __tests__/
│   │   │       └── workspaceApiClient.test.ts
│   │   ├── storage/
│   │   │   ├── workspaceLocalStorage.ts    # Local storage adapter
│   │   │   └── __tests__/
│   │   │       └── workspaceLocalStorage.test.ts
│   │   └── http/
│   │       └── workspaceHttpClient.ts      # HTTP client wrapper
│   └── presentation/
│       ├── components/
│       │   ├── WorkspaceSelector.vue       # Enhanced workspace selector component
│       │   ├── WorkspaceSelectorItem.vue   # Individual workspace item
│       │   ├── WorkspaceLoadingState.vue   # Loading skeleton
│       │   ├── WorkspaceErrorState.vue     # Error display
│       │   └── __tests__/
│       │       ├── WorkspaceSelector.test.ts
│       │       ├── WorkspaceSelectorItem.test.ts
│       │       ├── WorkspaceLoadingState.test.ts
│       │       └── WorkspaceErrorState.test.ts
│       └── composables/
│           └── useWorkspaceSelectorUI.ts   # UI-specific composable

client/e2e/
└── workspace-selection.spec.ts             # E2E tests for workspace selection flows

server/engine/src/main/kotlin/com/cvix/engine/workspace/
└── (READ-ONLY - Backend already implemented, no changes needed)
```

**Structure Decision**: Web application structure (frontend focus). The workspace feature follows Hexagonal Architecture principles adapted for Vue/TypeScript:

- **domain/**: Pure TypeScript business logic (no Vue, no framework dependencies)
- **application/**: Composables that orchestrate domain logic (framework-agnostic where possible)
- **infrastructure/**: Framework-specific adapters (Pinia, API clients, storage)
- **presentation/**: Vue components and UI-specific logic

This mirrors the backend's domain/application/infrastructure structure for consistency across the codebase.

## Complexity Tracking

Not applicable - all constitution gates passed.

## Completion Summary

### Artifacts Generated

All planning artifacts have been successfully created:

1. **research.md** (Phase 0) - 10 research topics with decisions and rationale:
   - Backend API integration strategy
   - State management architecture (Pinia)
   - Persistence approach (local storage)
   - Component architecture (compound pattern with Shadcn-Vue)
   - Auto-load on login implementation (Vue Router guards)
   - Performance optimization (5min cache TTL, lazy loading)
   - Accessibility requirements (ARIA, keyboard nav)
   - Internationalization setup (vue-i18n)
   - Testing strategy (70% unit, 20% integration, 10% E2E)

2. **data-model.md** (Phase 1) - Complete domain model:
   - 3 core entities: Workspace, WorkspaceSelectionPreference, WorkspaceState
   - 2 value objects with validation: WorkspaceId (UUID), WorkspaceName (1-100 chars)
   - 1 domain service: WorkspaceSelectionService (workspace selection logic)
   - State transition diagram (7 states: INITIAL → LOADING → LOADED → SWITCHING → SELECTED → ERROR)
   - API response models: GetAllWorkspacesResponse, GetWorkspaceResponse, ErrorResponse
   - Local storage schema
   - 7 error codes defined

3. **contracts/workspace-api.yaml** (Phase 1) - OpenAPI 3.0 specification:
   - GET /api/workspace endpoint (retrieve all workspaces)
   - GET /api/workspace/{workspaceId} endpoint (retrieve specific workspace)
   - Authentication requirements (Bearer JWT token)
   - Complete request/response schemas
   - Error response definitions (401, 403, 404, 500)

4. **quickstart.md** (Phase 1) - Developer guide:
   - Prerequisites and installation steps
   - Basic usage examples (composable, Pinia store, component)
   - Integration steps (store registration, navigation guard, layout integration)
   - Testing commands (unit, integration, E2E)
   - Configuration (environment variables, local storage)
   - Troubleshooting section (4 common issues with solutions)
   - API reference quick overview

5. **Agent context updated** - GitHub Copilot instructions file updated with:
   - TypeScript 5.x and Kotlin 2.0.20 (read-only)
   - Vue.js 3.5.17+, Pinia 3.0.3+, Vite 7.0.4+, Tailwind CSS 4.1.11+, Shadcn-Vue
   - Local Storage and Backend API integration

### Constitution Check Result

✅ **All 7 constitution principles validated and passed (Pre-design check)**

**Post-Design Re-evaluation**:

After completing research, data model, and API contracts, all constitution gates remain compliant:

- ✅ **I. Hexagonal Architecture**: Design confirms clean separation - domain service (WorkspaceSelectionService) contains business logic, composables orchestrate, infrastructure adapters (API client, storage, Pinia store) are isolated
- ✅ **II. Test-Driven Development**: Test structure defined in research.md achieves 75% coverage target (70% unit, 20% integration, 10% E2E), test files co-located with implementation
- ✅ **III. Code Quality**: Data model shows TypeScript interfaces with strict typing, validation in value objects (WorkspaceId, WorkspaceName), JSDoc comments planned
- ✅ **IV. Security First**: API contracts show Bearer token authentication, backend authorization enforced, no sensitive data in local storage (only workspace ID)
- ✅ **V. User Experience**: Quickstart shows Shadcn-Vue component usage, ARIA labels in research, loading states (WorkspaceLoadingState.vue), error states (WorkspaceErrorState.vue)
- ✅ **VI. Performance**: Research confirms 5min cache TTL, lazy loading strategy, <10KB bundle target, <2s load / <3s switch targets achievable with optimistic UI
- ✅ **VII. Observability**: Error codes defined (7 codes in data-model.md), state transitions documented, logging points identified in domain service

**Design Impact**: No constitution violations introduced. All principles reinforced by detailed design artifacts.

### Next Steps

The planning phase is complete. Proceed to task breakdown:

```bash
# Generate tasks.md with detailed implementation tasks
/speckit.tasks
```

The task breakdown will generate:

- Phase 1: Setup tasks (infrastructure, types, scaffolding)
- Phase 2: Foundational tasks (domain service, API client, Pinia store, storage adapter)
- Phase 3+: User story implementation tasks (US-001, US-002, US-003)

Each task will include:

- Test-driven workflow steps (🧪 → 🔴 → ♻️ → ✅)
- Acceptance criteria
- File paths and dependencies
- Estimated complexity
