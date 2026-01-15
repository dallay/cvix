# Implementation Plan: PDF Section Selector

**Branch**: `005-pdf-section-selector` | **Date**: 2025-12-06 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-pdf-section-selector/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See
`.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Redesign the PDF Generator screen to allow users to select which sections and individual items
appear in the final PDF resume. This enables users to tailor their resume content for specific job
applications without modifying source data. The implementation involves adding toggleable section
pills above the preview area, with expandable item-level controls, real-time preview updates, and
preference persistence.

## Technical Context

**Language/Version**: Kotlin 2.0.20 (backend), TypeScript 5.x (frontend)
**Primary Dependencies**: Spring Boot 3.3.4 with WebFlux (backend), Vue.js 3.5.17 with Composition
API, Pinia 3.0.3, TailwindCSS 4.1.11 (frontend)
**Storage**: PostgreSQL with R2DBC (backend), SessionStorage/LocalStorage (frontend preference
persistence)
**Testing**: JUnit 5, Kotest, Testcontainers (backend), Vitest, @testing-library/vue, Playwright (
frontend)
**Target Platform**: Web application (Chrome, Firefox, Safari, Edge), responsive from 768px to 2560px (tablet and larger devices only)
**Project Type**: Web (monorepo with backend + frontend)
**Performance Goals**: Section toggle UI response < 100ms, PDF generation with filtered content
maintains existing p95 < 500ms
**Constraints**: Preference persistence for 30+ days, mobile-responsive (tablet+ starting at 768px minimum), maintain
existing API backward compatibility
**Scale/Scope**: Single feature within existing Resume module, affects ~5 frontend components, ~3
backend modifications

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Code Quality First ✅

- **Compliance**: Will follow Kotlin/TypeScript conventions per `.agents/` docs
- **Static Analysis**: Detekt (Kotlin), Biome (TypeScript) will be applied
- **Immutability**: Section preferences will use immutable data structures
- **Documentation**: All new public APIs will have JSDoc/KDoc

### II. Testing Standards ✅

- **Unit Tests**: Section preference logic, filter functions, Vue components
- **Integration Tests**: Store ↔ LocalStorage, API ↔ filtered resume generation
- **E2E Tests**: Full user flow for section toggling and PDF export
- **Naming**: `should toggle section visibility when clicking pill`

### III. User Experience Consistency ✅

- **Design System**: Uses semantic tokens (`--primary`, `--muted`, etc.) per Figma design
- **Keyboard Accessible**: All toggle pills will be keyboard navigable
- **Form Validation**: Manual validation on blur pattern maintained
- **i18n**: All labels wrapped with `$t()` function

### IV. Performance Requirements ✅

- **UI Response**: Toggle actions will update UI in < 100ms (reactive Vue)
- **API Response**: Filtered resume generation maintains p95 < 500ms
- **No Blocking**: Frontend-only filtering, no blocking operations
- **Caching**: Preference persistence uses localStorage for 30-day retention

### Gate Status: **PASS** - No constitution violations identified

## Project Structure

### Documentation (this feature)

```text
specs/005-pdf-section-selector/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
# Frontend (Vue.js)
client/apps/webapp/src/core/resume/
├── domain/
│   ├── Resume.ts                           # Existing - resume data model
│   └── SectionVisibility.ts                # NEW - section/item visibility preferences model
├── application/
│   └── ResumeSectionFilterService.ts       # NEW - filter resume based on preferences
├── infrastructure/
│   ├── presentation/
│   │   ├── pages/
│   │   │   └── ResumePdfPage.vue           # MODIFIED - add visible sections control
│   │   ├── components/
│   │   │   ├── SectionTogglePill.vue       # NEW - toggleable pill component
│   │   │   ├── SectionTogglePanel.vue      # NEW - panel containing all section pills
│   │   │   ├── ItemToggleList.vue          # NEW - expandable item list for section
│   │   │   └── resume-preview/             # MODIFIED - consume filtered resume
│   │   └── composables/
│   │       └── useSectionVisibility.ts     # NEW - composable for visibility state management
│   ├── store/
│   │   ├── resume.store.ts                 # MODIFIED - add section visibility state
│   │   └── section-visibility.store.ts     # NEW - dedicated visibility preferences store
│   └── storage/
│       └── SectionVisibilityStorage.ts     # NEW - localStorage persistence for preferences

client/packages/ui/src/components/ui/
└── (existing Shadcn components used: toggle, badge, collapsible)

# Backend (Kotlin/Spring Boot)
server/engine/src/main/kotlin/com/cvix/resume/
├── domain/
│   └── (no changes - filtering is frontend-only)
├── application/
│   └── generate/
│       └── (no changes - receives pre-filtered resume from frontend)
└── infrastructure/
    └── http/
        └── (no changes - API contract unchanged)
```

**Structure Decision**: Web application (Option 2) - Monorepo with frontend and backend. The section
filtering is implemented entirely on the frontend to provide instant visual feedback. The backend
receives pre-filtered resume data for PDF generation, maintaining backward compatibility with
existing API contracts.

## Complexity Tracking

> No constitution violations requiring justification.

| Violation | Why Needed | Simpler Alternative Rejected Because |
| --------- | ---------- | ------------------------------------ |
| N/A       | N/A        | N/A                                  |

## 4. SectionType (Enum)

**Note**: Section ordering is hardcoded in the backend LaTeX template (`engineering.stg`).
