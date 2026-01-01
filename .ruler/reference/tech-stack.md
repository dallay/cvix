# Technology Stack

> Summary of the languages, frameworks, and tools used in the project.

## Backend Stack

| Category                | Technology                                  | Version |
|-------------------------|---------------------------------------------|---------|
| **Language**            | Kotlin                                      | 2.0.20  |
| **Framework**           | Spring Boot with Spring WebFlux (reactive)  | 3.3.4   |
| **Database**            | PostgreSQL with Spring Data R2DBC           | -       |
| **Security**            | Spring Security with OAuth2 Resource Server | -       |
| **Authentication**      | Keycloak for SSO and user management        | 26.0.0  |
| **API Documentation**   | SpringDoc OpenAPI                           | 2.6.0   |
| **Database Migrations** | Liquibase                                   | -       |
| **Testing**             | JUnit 5, Kotest, Testcontainers, MockK      | -       |
| **Build Tool**          | Gradle with Kotlin DSL                      | 9.x     |

---

## Frontend Stack

| Category             | Technology                           | Version |
|----------------------|--------------------------------------|---------|
| **Web App**          | Vue.js with TypeScript               | 3.5.17  |
| **Landing Page**     | Astro with Vue components            | 5.11.1  |
| **Styling**          | TailwindCSS                          | 4.1.11  |
| **State Management** | Pinia                                | 3.0.3   |
| **Form Validation**  | Vee-Validate with Zod schemas        | -       |
| **UI Components**    | Reka UI, Lucide icons, vue-shadcn UI | -       |
| **Build Tool**       | Vite                                 | 7.0.4   |
| **Package Manager**  | pnpm (monorepo with workspaces)      | 10.27.0 |

> **Warning:** pnpm@10.27.0 is **required** for this monorepo. This version introduces a breaking change in the virtual store layout: all unscoped packages now reside under an "@/" directory, which may affect tooling or scripts that depend on the old store structure.
>
> **Action required:** Upgrade to pnpm@10.27.0 or newer, run `pnpm store prune`, and perform a fresh install to rebuild node_modules with the new layout. For details, see the [pnpm 10.27.0 release notes](https://github.com/pnpm/pnpm/releases/tag/v10.27.0) and [virtual store re-organization](https://github.com/pnpm/pnpm/releases/tag/v10.24.0).

---

## Infrastructure & DevOps

| Category             | Technology                           |
|----------------------|--------------------------------------|
| **Containerization** | Docker Compose for local development |
| **Database**         | PostgreSQL with Docker               |
| **CI/CD**            | GitHub Actions                       |

---

## Code Quality & Testing

| Category                | Technology                      |
|-------------------------|---------------------------------|
| **Linting (JS/TS)**     | Biome                           |
| **Linting (Kotlin)**    | Detekt                          |
| **Frontend Testing**    | Vitest (unit), Playwright (E2E) |
| **Backend Testing**     | JUnit/Kotest                    |
| **Coverage (Kotlin)**   | Kover                           |
| **Coverage (Frontend)** | Vitest coverage                 |
| **Security**            | OWASP Dependency Check          |
| **Git Hooks**           | Lefthook                        |

---

## Key Dependencies Summary

### Backend

```text
- org.springframework.boot:spring-boot-starter-webflux
- org.springframework.boot:spring-boot-starter-data-r2dbc
- org.springframework.boot:spring-boot-starter-security
- org.springframework.boot:spring-boot-starter-oauth2-resource-server
- org.springframework.boot:spring-boot-starter-actuator
- org.liquibase:liquibase-core
- io.r2dbc:r2dbc-postgresql
- io.mockk:mockk
- io.kotest:kotest-assertions-core
- org.testcontainers:postgresql
```

### Frontend

```text
- vue@^3.5.17
- pinia@^3.0.3
- vee-validate@^4.x
- zod@^3.x
- @vueuse/core
- tailwindcss@^4.1.11
- @tanstack/vue-query (if used)
- lucide-vue-next
```
