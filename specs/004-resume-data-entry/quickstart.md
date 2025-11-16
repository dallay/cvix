# Quickstart — Resume Data Entry Screen

This guide explains how to run the resume editor (frontend) and optional persistence service (backend) locally.

## Prerequisites

- JDK 21+
- pnpm >= 10
- Docker + Docker Compose

## 1) Start infrastructure

```bash
# From repo root
docker compose up -d postgresql keycloak
```

## 2) Backend (optional persistence)

```bash
# Run full backend build
./gradlew build

# Start the Spring Boot app (engine)
./gradlew :server:engine:bootRun
```

Notes:

- Optional API served under `/api/resume`.
- Requires Keycloak; configure `issuer-uri` via application.yml.

## 3) Frontend (resume editor)

```bash
# Install dependencies at repo root
pnpm install

# Launch the web app (only webapp)
pnpm --filter @loomify/webapp dev

# Or start all dev servers configured
pnpm run dev
```

Navigate to the Resume Editor via the app menu (feature route). You should see the split view: form on the left, preview on the right.

## 4) JSON Import & Export

- Upload JSON: Top bar → "Upload JSON Resume" → choose `.json` file
- Export JSON: Top bar → "Download JSON Resume" (Cmd/Ctrl+S)
- Validate JSON: Top bar → "Validate JSON" (bottom drawer opens)

## 5) Autosave

- Edits autosave to IndexedDB within ~2s.
- Multi-tab editing synced via BroadcastChannel (last-write-wins).
- Reset Form clears local storage after confirmation.

## 6) PDF Generation

- Open the PDF screen via "Generate PDF" navigation.
- Select a template and preview updates.
- Click "Generate PDF" → then "Download PDF" to save.

## 7) Tests

```bash
# Frontend unit/integration tests
pnpm -C client test

# Backend tests
./gradlew test

# E2E tests (ensure dev servers are running)
pnpm -C client test:e2e
```

## 8) Linting & Quality Gates

```bash
# Frontend linting
pnpm check

# Backend static analysis
./gradlew detektAll

# Dependency & security checks
./gradlew dependencyCheckAnalyze
```
