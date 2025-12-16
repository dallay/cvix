# Project Overview

This is a production-ready monorepo template for building SaaS applications. It includes a Spring Boot + Kotlin backend, modern frontend apps (Vite / Astro / Vue), and standardized infrastructure with Docker Compose. The goal is to provide a solid foundation for engineering teams to build upon, with batteries-included for CI, linting, testing, and security.

## Documentation Structure

```text
.ruler/
├── general.md              # Git, code review, documentation guidelines (always loaded)
├── testing.md              # Testing strategy and conventions (always loaded)
├── backend/                # Backend-specific conventions
│   ├── kotlin.md           # Kotlin language conventions
│   ├── spring-boot.md      # Spring Boot & WebFlux conventions
│   ├── api.md              # REST API design guidelines
│   └── database.md         # Database guidelines (UUID, RLS, Liquibase)
├── frontend/               # Frontend-specific conventions
│   ├── typescript.md       # TypeScript conventions
│   ├── vue.md              # Vue 3 conventions
│   ├── astro.md            # Astro conventions
│   ├── html-css.md         # HTML & CSS conventions
│   └── design-system.md    # Design tokens and styling
├── reference/              # Detailed documentation
│   ├── architecture.md     # Hexagonal architecture details
│   ├── project-structure.md # Monorepo structure
│   └── tech-stack.md       # Technology stack summary
└── sop/                    # Standard Operating Procedures
    ├── adding-migrations.md     # How to add database migrations
    └── adding-api-endpoint.md   # How to add new API endpoints
```

## Quick Reference

| Task | Reference |
|------|-----------|
| Writing Kotlin code | `backend/kotlin.md` |
| Creating API endpoints | `sop/adding-api-endpoint.md` + `backend/api.md` |
| Database changes | `sop/adding-migrations.md` + `backend/database.md` |
| Vue components | `frontend/vue.md` |
| Styling | `frontend/design-system.md` + `frontend/html-css.md` |
| Testing | `testing.md` |
| Git workflow | `general.md` |
