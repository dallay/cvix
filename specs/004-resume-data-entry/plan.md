# Implementation Plan: Resume Data Entry Screen

**Branch**: `004-resume-data-entry` | **Date**: 2025-11-16 | **Spec**: [/specs/004-resume-data-entry/spec.md](/specs/004-resume-data-entry/spec.md)
**Input**: Feature specification from `/specs/004-resume-data-entry/spec.md`

## Summary

Deliver a two-column resume data entry experience backed by the JSON Resume schema with real-time, debounced preview and robust import/export. Provide a separate PDF generation module with template selection (UI pending) leveraging existing backend LaTeX PDF generation. Persist data mandatorily to backend PostgreSQL (JSONB) with complementary local IndexedDB autosave for draft/offline support. Follow Hexagonal Architecture on the backend and clean, screaming architecture on the frontend.

## Technical Context

**Language/Version**: Backend: Kotlin 2.x + Spring Boot 3.3 (WebFlux, R2DBC); Frontend: Vue 3 + TypeScript (Vite)
**Primary Dependencies**: Frontend: Ajv v8 (+ ajv-formats, ajv-errors) for schema validation; `libphonenumber-js` for phone validation; internal debounce utility (`client/packages/utilities/src/debounce/debounce.ts`); optional `@vueuse/core` for shortcuts. Backend: Spring WebFlux, Spring Security, Spring Data R2DBC, PostgreSQL; existing LaTeX-based PDF generator service.
**Storage**: Mandatory server persistence in PostgreSQL (`resumes` JSONB table) plus local IndexedDB (`idb-keyval`) for fast draft autosave and offline resilience.
**Testing**: Backend: JUnit 5 + Kotest + MockK + Testcontainers; Frontend: Vitest + @testing-library/vue; E2E: Playwright.
**Target Platform**: Web app (desktop-first responsive), JVM microservice, PostgreSQL via Docker Compose.
**Project Type**: Monorepo with `server/` (Gradle multi-project) and `client/` (pnpm workspaces).
**Performance Goals**: Live preview updates within 150ms; Template switch within 500ms; JSON import/export within 2s; server persistence save round‑trip < 400ms p95 (local dev); handle up to 50 work entries without UI jank.
**Constraints**: Must adhere to Hexagonal architecture, TDD, coverage gates (backend 80%+, domain 100%; frontend 75%+). Avoid blocking calls in WebFlux. No `any` in TS. Use design tokens.
**Scale/Scope**: Single-user local editing by default; optional authenticated persistence for 10k+ users in future without redesign (JSONB allows schema evolution).

NEEDS CLARIFICATION (Phase 0 to resolve):

- JSON Resume schema version pin (proposed: 1.0.0) and commit hash.
- Template enumeration endpoint & model (proposed: `GET /api/resume-templates`).
- Internationalization scope for resume content labels (out of scope for MVP?).

## Constitution Check

Gate items derived from `.specify/memory/constitution.md`:

- Hexagonal Architecture: Backend must separate domain, application (CQRS), and infrastructure. Adapters implement ports.
- TDD Mandatory: Write tests first. Maintain pyramid: unit > integration > E2E. Coverage: backend 80%+ (domain 100%), frontend 75%+.
- Code Quality: Detekt and Biome pass with zero violations. Kotlin expression body for one-statement functions. No `!!`, no TS `any`.
- Security: Parameterized queries (R2DBC), no secrets in code, security headers in prod, validate all inputs.
- UX Consistency: Use design tokens, accessible UI, keyboard shortcuts, sticky TOC, debounce limits respected.

Initial Gate Evaluation: PASS with conditions

- Architectural plan conforms to Hexagonal (backend) and clean architecture (frontend).
- Test approach defined per layer and tools chosen per constitution.
- Performance targets match Success Criteria in spec.
- Open risks (PDF engine, storage scope) flagged for Phase 0 research and must be resolved before implementation.

Post-Design Recheck: PASS

- Phase 0 research resolved open topics (schema pin, PDF strategy, storage).
- Phase 1 artifacts created (data model, contracts, quickstart) and align with constitution (Hexagonal, TDD, quality gates).
- No violations require justification; optional trade-offs recorded in Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/004-resume-data-entry/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
  └── openapi.yaml
```

### Source Code (repository root)

```text
server/
└── engine/
  └── src/
    ├── main/kotlin/com/loomify/resume/
    │   ├── domain/               # Entities, value objects, ports
    │   ├── application/          # Use cases (CQRS)
    │   └── infrastructure/
    │       ├── http/             # REST controllers (persistence + PDF)
    │       ├── persistence/      # R2DBC repos, JSONB mapping
    │       ├── pdf/              # LaTeX template service adapter
    │       └── config/           # Beans, security
    └── test/kotlin/com/loomify/resume/
        ├── unit/
        ├── integration/
        └── contract/

client/
└── apps/webapp/src/core/resume/
    ├── pages/
    │   ├── ResumeEditorPage.vue      # Split view: form + preview
    │   └── ResumePdfPage.vue         # PDF template selection + generate
    ├── components/
    │   ├── ResumeToc.vue
    │   ├── ResumeBasicsForm.vue
    │   ├── ResumeWorkForm.vue
    │   ├── ResumeEducationForm.vue
    │   ├── ResumeSkillsForm.vue
    │   ├── ResumeProjectsForm.vue
    │   ├── ResumeLanguagesForm.vue
    │   ├── ResumeCertificatesForm.vue
    │   ├── ResumeOptionalSections.vue
    │   ├── ResumePreview.vue         # Debounced preview (internal utility)
    │   └── PdfTemplateSelector.vue   # UI to choose template (to implement)
    ├── stores/
    │   └── resume.store.ts           # Pinia store, sync local + server
    ├── composables/
    │   ├── useJsonResume.ts          # Import/export, Ajv validation
    │   ├── useAutosave.ts            # IndexedDB + BroadcastChannel
    │   ├── usePersistence.ts         # Server CRUD (JSONB)
    │   └── usePdf.ts                 # Calls backend PDF endpoint
    ├── types/
    │   └── json-resume.ts            # Generated from schema
    └── validators/
        └── json-resume.schema.json   # Pinned schema for Ajv
tests/
  ├── unit/
  ├── integration/
  └── e2e/ (client/e2e)
```

**Structure Decision**: Web application with mandatory backend API. Frontend houses feature in `client/apps/webapp/src/core/resume`. Backend module implements persistence + PDF generation adapters. Persistence is mandatory: all saved resumes stored server-side (JSONB). Local storage is supplemental for autosave/offline.

## Complexity Tracking

| Violation                                                         | Why Needed                                                                                                                                                         | Simpler Alternative Rejected Because                                                                                                                                                                                                     |
|-------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| JSONB persistence (mandatory)                                     | Flexible evolving schema; nested arrays                                                                                                                            | Normalization raises migration churn and fragments nested list semantics                                                                                                                                                                 |
| TDD-optional approach (tests in Phase 9 vs before implementation) | Rapid prototyping for user feedback on complex UI interactions before test investment; constitution mandates TDD but stakeholder requested working prototype first | Writing comprehensive tests before finalizing UX design would result in significant test rework; tests added in polish phase to validate finalized behavior and ensure coverage gates (80% backend, 75% frontend) are met before release |
