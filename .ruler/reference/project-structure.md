# Project Structure

> High-level overview of the monorepo's directory structure.

## Root Level Organization

```text
├── client/                 # Frontend monorepo (PNPM workspace)
├── server/                 # Backend services (Gradle multi-project)
├── shared/                 # Shared libraries (Kotlin)
├── infra/                  # Infrastructure as Code (Docker, etc.)
├── docs/                   # Project documentation (Astro)
├── config/                 # Build and quality configs (Detekt, OWASP)
└── gradle/                 # Gradle wrapper and configuration
```

---

## Backend Structure (`server/`)

```text
server/
└── engine/                 # Main Spring Boot application
    └── src/
        ├── main/
        │   ├── kotlin/     # Application code
        │   └── resources/
        │       └── db/
        │           └── changelog/  # Liquibase migrations
        └── test/
            └── kotlin/     # Test code
```

- **`server/engine`**: Main Spring Boot application
- Packages organized by **domain-driven design** principles
- Migrations located in `src/main/resources/db/changelog/`

---

## Frontend Structure (`client/`)

```text
client/
├── apps/
│   ├── webapp/             # Main Vue.js SPA
│   │   ├── src/
│   │   │   ├── components/
│   │   │   ├── composables/
│   │   │   ├── layouts/
│   │   │   ├── pages/
│   │   │   ├── stores/
│   │   │   └── styles/
│   │   └── ...
│   └── marketing/          # Astro-based marketing site
│       ├── src/
│       │   ├── components/
│       │   ├── content/
│       │   ├── layouts/
│       │   └── pages/
│       └── ...
├── packages/
│   ├── ui/                 # Shared UI components (Shadcn-Vue)
│   ├── assets/             # Shared assets
│   └── tsconfig/           # Shared TypeScript configurations
└── e2e/                    # Playwright E2E tests
```

| Directory               | Purpose                                                    |
|-------------------------|------------------------------------------------------------|
| `client/apps/webapp`    | Main Vue.js SPA                                            |
| `client/apps/marketing` | Astro-based marketing and landing page site                |
| `client/packages/ui`    | Shared UI components (Shadcn-Vue)                          |
| `client/packages/*`     | Shared frontend code, utilities, TypeScript configurations |
| `client/e2e`            | End-to-end tests with Playwright                           |

---

## Shared Libraries (`shared/`)

```text
shared/
├── common/                 # Common Kotlin utilities
│   └── src/
│       ├── main/kotlin/
│       └── test/kotlin/
└── spring-boot-common/     # Shared Spring Boot components
    └── src/
        ├── main/kotlin/
        └── test/kotlin/
```

| Directory                   | Purpose                                        |
|-----------------------------|------------------------------------------------|
| `shared/common`             | Common Kotlin utilities (domain-agnostic)      |
| `shared/spring-boot-common` | Shared components for Spring Boot applications |

---

## Infrastructure (`infra/`)

```text
infra/
├── grafana/                # Grafana dashboards
├── keycloak/               # Keycloak realm configuration
├── maildev/                # Local mail server for development
├── postgresql/             # PostgreSQL initialization scripts
├── prometheus/             # Prometheus alerts configuration
└── secrets/                # Secret management documentation
```

---

## Configuration (`config/`)

```text
config/
└── owasp/
    └── owasp-suppression.xml   # OWASP dependency check suppressions
└── detekt.yml                  # Detekt (Kotlin linter) configuration
```

---

## Documentation (`.ruler/`)

```text
.ruler/
├── general.md              # Git, code review, documentation guidelines
├── testing.md              # Testing strategy and conventions
├── backend/                # Backend-specific conventions
│   ├── kotlin.md
│   ├── spring-boot.md
│   ├── api.md
│   └── database.md
├── frontend/               # Frontend-specific conventions
│   ├── typescript.md
│   ├── vue.md
│   ├── astro.md
│   ├── html-css.md
│   └── design-system.md
├── reference/              # Detailed documentation
│   ├── architecture.md
│   ├── project-structure.md
│   └── tech-stack.md
└── sop/                    # Standard Operating Procedures
    ├── adding-migrations.md
    └── adding-api-endpoint.md
```
