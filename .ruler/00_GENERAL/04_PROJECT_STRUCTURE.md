# Project Structure

> A high-level overview of the monorepo's directory structure.

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

## Backend Structure (`server/`)

- **`server/engine`**: Main Spring Boot application.
- Packages are organized by domain-driven design principles.
- Migrations are located in `src/main/resources/db/changelog/`.

## Frontend Structure (`client/`)

- **`client/apps/webapp`**: The main Vue.js SPA.
- **`client/apps/marketing`**: The Astro-based marketing and landing page site.
- **`client/packages/*`**: Shared frontend code, such as utilities and TypeScript configurations.

## Shared Libraries (`shared/`)

- **`shared/common`**: Common Kotlin utilities.
- **`shared/spring-boot-common`**: Shared components for Spring Boot applications.
