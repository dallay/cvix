# Implementation Plan: Resume Data Entry Screen

**Branch**: `004-resume-data-entry` | **Date**: 2025-11-16 | **Spec**: [/specs/004-resume-data-entry/spec.md](/specs/004-resume-data-entry/spec.md)
**Input**: Feature specification from `/specs/004-resume-data-entry/spec.md`

## Summary

Deliver a two-column resume data entry experience backed by the JSON Resume schema with real-time, debounced preview and robust import/export. Provide a separate PDF generation module with template selection. Persist locally via IndexedDB (autosave) and optionally via backend storage (JSONB) for authenticated users. Follow Hexagonal Architecture on the backend and clean, screaming architecture on the frontend.

## Technical Context

**Language/Version**: Backend: Kotlin 2.x + Spring Boot 3.3 (WebFlux, R2DBC); Frontend: Vue 3 + TypeScript (Vite)
**Primary Dependencies**: Frontend: Ajv v8 (+ ajv-formats, ajv-errors) for schema validation; `libphonenumber-js` for phone validation; `lodash.debounce`; optional `@vueuse/core` for shortcuts; `html2pdf.js` for initial client-side PDF. Backend: Spring WebFlux, Spring Security, Spring Data R2DBC, PostgreSQL.
**Storage**: Local (IndexedDB via `idb-keyval`) for autosave; Optional server persistence in PostgreSQL using a `resumes` table with JSONB payload and owner scoping.
**Testing**: Backend: JUnit 5 + Kotest + MockK + Testcontainers; Frontend: Vitest + @testing-library/vue; E2E: Playwright.
**Target Platform**: Web app (desktop-first responsive), JVM microservice, PostgreSQL via Docker Compose.
**Project Type**: Monorepo with `server/` (Gradle multi-project) and `client/` (pnpm workspaces).
**Performance Goals**: Live preview updates within 150ms; Template switch within 500ms; JSON import/export within 2s; Handle up to 50 work entries without UI jank.
**Constraints**: Must adhere to Hexagonal architecture, TDD, coverage gates (backend 80%+, domain 100%; frontend 75%+). Avoid blocking calls in WebFlux. No `any` in TS. Use design tokens.
**Scale/Scope**: Single-user local editing by default; optional authenticated persistence for 10k+ users in future without redesign (JSONB allows schema evolution).

NEEDS CLARIFICATION (Phase 0 to resolve):

- JSON Resume schema version pin (proposed: 1.0.0) and commit hash.
- Client-only PDF vs. server-rendered PDF (initial: client; future: server using Playwright).
- Storage strategy: local-only vs. optional server save behind feature flag.
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
    │       ├── http/             # REST controllers (optional MVP)
    │       ├── persistence/      # R2DBC repos, JSONB mapping
    │       └── config/            # Beans, security
    └── test/kotlin/com/loomify/resume/
      ├── unit/
      ├── integration/
      └── contract/

client/
└── apps/webapp/src/
  ├── features/resume/
  │   ├── pages/
  │   │   ├── ResumeEditorPage.vue    # Split view: form + preview
  │   │   └── ResumePdfPage.vue       # PDF generation screen
  │   ├── components/
  │   │   ├── ResumeToc.vue
+    │   │   ├── ResumeBasicsForm.vue
  │   │   ├── ResumeWorkForm.vue
  │   │   ├── ResumeEducationForm.vue
  │   │   ├── ResumeSkillsForm.vue
  │   │   ├── ResumeProjectsForm.vue
  │   │   ├── ResumeLanguagesForm.vue
  │   │   ├── ResumeCertificatesForm.vue
  │   │   ├── ResumeOptionalSections.vue
  │   │   ├── ResumePreview.vue       # Debounced preview
  │   │   └── PdfTemplateSelector.vue
  │   ├── stores/
  │   │   └── resume.store.ts         # Pinia store for form state
  │   ├── composables/
  │   │   ├── useJsonResume.ts        # Import/export, Ajv validation
  │   │   ├── useAutosave.ts          # IndexedDB persistence
  │   │   └── usePdf.ts               # html2pdf integration
  │   ├── types/
  │   │   └── json-resume.ts          # TS types aligned to schema
  │   └── validators/
  │       └── json-resume.schema.json # Pinned schema for Ajv
  └── tests/
    ├── unit/features/resume/
    ├── integration/features/resume/
    └── e2e/ (under client/e2e)
```

**Structure Decision**: Web application with optional backend API. Frontend houses feature in `client/apps/webapp/src/features/resume`. Backend adds modular package under `server/engine/src/main/kotlin/com/loomify/resume` following Hexagonal layers. Persistence initially local; backend persistence behind a feature flag and guarded endpoints.

## Complexity Tracking

| Violation                                   | Why Needed                                | Simpler Alternative Rejected Because                                                                      |
| ------------------------------------------- | ----------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| Client-side PDF (html2pdf) quality tradeoff | Enables MVP without server infra          | Server-rendered PDF via Playwright adds infra and latency; can be added later as template-quality upgrade |
| JSONB persistence (optional)                | Schema evolves rapidly; nested structures | Fully normalized schema increases migrations and complexity with little near-term benefit                 |
