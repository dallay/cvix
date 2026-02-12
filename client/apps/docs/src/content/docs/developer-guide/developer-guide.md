---
title: "Developer Guide"
description: "Guidance for contributors and developers working on the repo."
---

## Developer Guide

This guide covers the core development workflows, project structure, and standards for the ProFileTailors monorepo.

## Monorepo Structure

ProFileTailors is organized as a full-stack monorepo, facilitating shared types and streamlined development across the entire system.

```text
├── client/
│   ├── apps/
│   │   ├── webapp/      # Vue.js 3 main application (SPA)
│   │   ├── marketing/   # Astro-based landing page
│   │   ├── blog/        # Astro-based blog
│   │   └── docs/        # Starlight-based documentation site
│   └── packages/
│       ├── ui/          # Shared Shadcn-Vue components
│       ├── utilities/   # Shared TypeScript utilities
│       └── ...
├── server/
│   └── engine/          # Spring Boot 4.0 + Kotlin backend
├── shared/              # Kotlin shared libraries
├── infra/               # Infrastructure, Docker, monitoring, SSL
└── .agents/             # AI agent configurations and workflow guides
```

## Essential Workflows

We use a centralized `Makefile` to manage common tasks across both frontend and backend.

### Environment Setup

Always start by preparing your local environment:

```bash
make prepare-env
make install
make ssl-cert
```

### Development Servers

- **Full stack:** `make dev` (Starts all frontend apps)
- **Main Web App:** `make dev-web` (Vue SPA on port 9876)
- **Backend:** `make backend-run` (Spring Boot + PostgreSQL + Keycloak)
- **Documentation:** `make dev-docs` (Starlight on port 4321)

### Quality & Verification

Before opening a pull request, you **must** ensure the entire project passes verification:

```bash
make verify-all
```

This command runs:
- Frontend: `biome check`, `oxlint`, TypeScript type-checking, Vitest, and builds.
- Backend: `detektAll`, JUnit 5 tests, and builds.

## Coding Conventions

### Frontend (Vue/TypeScript)
- Use **Vue 3 Composition API** with `<script setup>`.
- **Strict TypeScript**: No `any` types.
- **Styling**: Tailwind CSS 4.
- **Linting**: Biome and oxlint.

### Backend (Kotlin/Spring Boot)
- **Hexagonal Architecture**: Maintain strict separation between domain, application, and infrastructure layers.
- **Reactive Stack**: Use WebFlux and R2DBC for non-blocking I/O.
- **Style**: 4 spaces, no wildcard imports, KDoc for public APIs.

For more detailed patterns and rules, refer to the guides in the `.agents/` directory, especially `.agents/AGENTS.md`.

## Testing Strategy

- **Backend**: JUnit 5 + Kotest + MockK. Use Testcontainers for integration tests.
- **Frontend**: Vitest for unit/component tests, Playwright for E2E tests.

---

For architecture deep-dives, check the `.agents/skills/` directory which contains specialized guides for Hexagonal Architecture, Spring Boot best practices, and more.
