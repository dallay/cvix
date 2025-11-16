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

## 5) PDF Generation Strategy (Current Backend Integration)

- Decision: Server-side LaTeX rendering pipeline (already implemented) invoked via backend; client sends selected template id + resume JSON; backend returns generated PDF.
- Rationale: High typographic fidelity, stable pagination, deterministic output across platforms; leverages existing infrastructure instead of duplicating a less consistent client-only approach.
- New Client Responsibility: Provide template selection UI and parameter editing; call backend generation endpoint and handle streaming/binary response.
- Alternatives considered:
  - Client-side `html2pdf.js`: rejected post-backend availability—lower fidelity, inconsistent fonts, larger client bundle.
  - Headless Chromium render service: viable alternative for future multi-template HTML designs but higher ops complexity vs current LaTeX pipeline.

## 6) Template System for Preview & PDF

- Decision: Client preview uses Vue components approximating LaTeX layout; PDF generation relies on backend LaTeX templates referenced by stable template ids.
- Rationale: Keeps interactive preview fast while delegating final formatting to robust backend pipeline; template ids create contract boundary.
- Alternatives considered:
  - Fully sharing LaTeX layout in client: impractical; LaTeX not directly renderable.
  - Distinct ad-hoc preview designs per template: increases drift risk.

## 7) Backend Persistence Model (Mandatory)

- Decision: Mandatory storage of full resume as JSONB in `resumes` table: `(id UUID PK, owner_id UUID, data JSONB, created_at, updated_at)` plus GIN index on `data`, optimistic locking via updated_at check on writes.
- Rationale: Ensures durability (server source of truth), supports future analytics/search and multi-device sync; JSONB provides evolution flexibility.
- Alternatives considered:
  - Fully normalized schema: high migration churn; complex joins for nested arrays.
  - Flat text blob: no indexing or structured querying.

## 15) Template Metadata Retrieval

- Decision: Expose `/api/templates` endpoint returning array of template metadata objects: `{ id, name, version, paramsSchema }`.
- Rationale: Centralized authoritative list; enables dynamic additions without frontend redeploy; paramsSchema guides client-side validation of configurable template parameters.
- Alternatives considered:
  - Hardcoded frontend list: requires redeploys for changes; risk of drift.
  - CDN manifest separate from API: splits authority; adds complexity.


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
