# Phase 0 Research — Resume Data Entry Screen

Status: Completed (all clarifications resolved)
Date: 2025-11-16

## 1) JSON Resume Schema Version

- Decision: Pin to JSON Resume schema v1.0.0 (commit hash to be recorded in validators/json-resume.schema.json header)
- Rationale: v1.0.0 is widely adopted and stable. Pinning avoids regressions from upstream changes.
- Alternatives considered:
  - Track `master` schema: rejected due to drift risk.
  - Custom fork: rejected; maintainability cost without specific needs.

## 2) Frontend Validation Library

- Decision: Use Ajv v8 with `ajv-formats` and `ajv-errors`
- Rationale: Fast, mature JSON Schema validator with good TS support; `ajv-formats` adds date/uri/email; `ajv-errors` improves UX messages.
- Alternatives considered:
  - Zod-only: great DX but would require duplicating JSON Schema; Ajv consumes official schema directly.
  - `yup` or custom validation: insufficient for schema-driven validation.

## 3) Phone Number Validation

- Decision: Use `libphonenumber-js` for client validation/formatting
- Rationale: Lightweight and accurate international phone parsing/formatting; can normalize to E.164.
- Alternatives considered:
  - Regex: brittle and locale-inaccurate.
  - Full `google-libphonenumber`: heavier bundle size.

## 4) Autosave Storage Strategy

- Decision: IndexedDB via `idb-keyval` with BroadcastChannel sync; key: `resume:draft`
- Rationale: Resilient for large payloads; atomic operations; BroadcastChannel enables multi-tab coherence and last-write-wins.
- Alternatives considered:
  - localStorage: synchronous and small capacity; risk of blocking main thread.
  - Dexie: powerful but heavier than needed for a single key/value.

## 5) PDF Generation Strategy (MVP)

- Decision: Client-side PDF using `html2pdf.js` (html2canvas + jsPDF) with print styles
- Rationale: No server dependency; good-enough fidelity for initial templates; fast iteration for template design.
- Alternatives considered:
  - Server-side Playwright/Chromium: superior fidelity, better fonts; deferred to Phase N as an optional service.
  - pdfmake: layout DSL but poor CSS compatibility; higher authoring cost.

## 6) Template System for Preview & PDF

- Decision: Shared Vue template components with print CSS; the Preview uses the same components as PDF (single source of truth)
- Rationale: Eliminates duplication and drift between preview and PDF; print styles drive PDF output.
- Alternatives considered:
  - Separate preview vs PDF templates: doubles maintenance; increases mismatch risk.

## 7) Backend Persistence Model (Optional)

- Decision: Store full resume as JSONB in `resumes` table: columns `(id UUID PK, owner_id UUID, data JSONB, created_at, updated_at)` with GIN index on `data`
- Rationale: Schema evolves; JSON Resume is hierarchic; JSONB avoids frequent migrations and preserves structure.
- Alternatives considered:
  - Fully normalized schema: high migration churn; complex queries for nested arrays.
  - Flat text blob: no queryability; no JSON operators.

## 8) Date Handling & Formats

- Decision: ISO 8601 dates (`YYYY-MM-DD`) everywhere; no time component in resume domain.
- Rationale: Matches JSON Resume common practice; simple comparisons.
- Alternatives considered:
  - Locale-formatted strings: ambiguous; error-prone.

## 9) Debounce Timing for Preview

- Decision: 120ms debounce on form → preview updates
- Rationale: Within 100–150ms target from spec; balances responsiveness and performance.
- Alternatives considered:
  - No debounce: costly rerenders on every keystroke.

## 10) Keyboard Shortcuts

- Decision: Use `@vueuse/core` `useMagicKeys` for cross-platform Cmd/Ctrl detection
- Rationale: Small and reliable; reduces key event boilerplate.
- Alternatives considered:
  - Manual keydown listeners: higher maintenance and edge-cases (IME, focus).

## 11) Accessibility & Navigation

- Decision: TOC uses semantic nav/aria-current; sections are accordions with proper roles; preview → form navigation via `scrollIntoView({ behavior: 'smooth', block: 'start' })` and focus management.
- Rationale: Meets a11y expectations; predictable keyboard navigation.
- Alternatives considered:
  - Custom div-only controls: harms accessibility.

## 12) Error Panel Behavior

- Decision: Global validation panel opens as bottom drawer; displays grouped errors with jump links; closes on resolve.
- Rationale: Non-modal keeps context; bottom placement matches spec.
- Alternatives considered:
  - Modal: blocks workflow; poor for long forms.

## 13) Internationalization Scope

- Decision: UI is i18n-ready; content (resume fields) is user-entered and unchanged. Provide translation keys for labels only in English for MVP; add locales later.
- Rationale: Focus on feature; enable future expansion with `vue-i18n`.
- Alternatives considered:
  - Full multi-locale at MVP: increases scope significantly.

## 14) Schema Type Safety in TS

- Decision: Generate TS types from JSON Schema once and check-in `json-resume.ts`; validate at runtime via Ajv.
- Rationale: Strong compile-time types + runtime validation; no drift.
- Alternatives considered:
  - Hand-written types only: risk of mismatch.
  - Runtime validation only: poorer DX and refactors.
