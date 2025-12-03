# Technology Stack

> A summary of the languages, frameworks, and tools used in the project.

## Backend Stack

- **Language**: Kotlin 2.0.20
- **Framework**: Spring Boot 3.3.4 with Spring WebFlux (reactive)
- **Database**: PostgreSQL with Spring Data R2DBC (reactive database access)
- **Security**: Spring Security with OAuth2 Resource Server
- **Authentication**: Keycloak 26.0.0 for SSO and user management
- **API Documentation**: SpringDoc OpenAPI 2.6.0
- **Database Migrations**: Liquibase
- **Testing**: JUnit 5, Kotest, Testcontainers, MockK
- **Build Tool**: Gradle 9.x with Kotlin DSL

## Frontend Stack

- **Web App**: Vue.js 3.5.17 with TypeScript
- **Landing Page**: Astro 5.11.1 with Vue components
- **Styling**: TailwindCSS 4.1.11
- **State Management**: Pinia 3.0.3
- **Form Validation**: Vee-Validate with Zod schemas
- **UI Components**: Reka UI, Lucide icons, vue-shadcn UI components
- **Build Tool**: Vite 7.0.4
- **Package Manager**: pnpm 10.13.1 (monorepo with workspaces)

## Infrastructure & DevOps

- **Containerization**: Docker Compose for local development
- **Database**: PostgreSQL with Docker
- **CI/CD**: GitHub Actions

## Code Quality & Testing

- **Linting**: Biome for JavaScript/TypeScript, Detekt for Kotlin
- **Testing**: Vitest for frontend (unit test) and Playwright (e2e), JUnit/Kotest for backend
- **Coverage**: Kover for Kotlin, Vitest coverage for frontend
- **Security**: OWASP Dependency Check
- **Git Hooks**: Lefthook for Git-hooks management
