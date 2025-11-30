# SaaS Template — Product Overview

## One-line summary

A production-ready monorepo template for building SaaS applications: Spring Boot + Kotlin backend, modern frontend apps (Vite / Astro / Vue), standardized infra with Docker Compose, and batteries-included developer tooling for CI, linting, testing, and security.

## Purpose and target audience

This repository is a starter kit aimed at engineering teams building subscription-style web applications. It provides a reproducible foundation for authentication, email, database migrations, monitoring, CI/CD, and developer productivity tooling so teams can focus on product features instead of boilerplate.

## High-level architecture

- Backend: Spring Boot (Kotlin) services under `server/` (engine and related modules). Gradle is used to build, test, and run the JVM services.
- Frontend: A monorepo under `client/` hosting multiple web apps and packages (marketing, web, docs). Uses Vite, Astro and related tooling.
- Infrastructure: Docker Compose definitions in the repo root (`compose.yaml`) and dedicated infra snippets for Postgres, Keycloak, GreenMail, Maildev, and local TLS certs in `infra/`.
- CI/CD: GitHub Actions workflows live under `.github/workflows` and custom actions in `.github/actions`.
- Developer utilities: `lefthook` for git hooks, `ruler` for repository governance, Biome for JavaScript linting, Detekt for Kotlin static analysis, and Liquibase for DB migrations.

## Tech stack

- Kotlin + Spring Boot (backend)
- Gradle Kotlin DSL (`build.gradle.kts`) and Gradle Wrapper
- Vite, Astro, Vue (frontend apps)
- PostgreSQL (database), Liquibase (migrations)
- Keycloak (auth), GreenMail / MailDev (email testing)
- Testcontainers for integration tests
- PNPM for JS workspace management

## Important repo files & where to look

- `README.md` — high-level developer instructions and Compose references.
- `build.gradle.kts` (root) — Gradle plugin configuration and project defaults.
- `package.json` (root) — pnpm workspace, scripts and developer tooling configuration.
- `compose.yaml` — local infrastructure used for development.
- `infra/` — environment composition and helper scripts.
- `server/` — backend applications (Gradle subprojects).
- `client/` — frontend apps and shared packages.
- `.github/workflows/` — CI/CD flows.
- `config/` and `docs/` — configuration and documentation sites.

## Quickstart (developer)

1. Prerequisites: JDK 21+, pnpm >= 10, Docker & Docker Compose, Git.
2. Install JS deps: `pnpm install` (root uses `pnpm` and enforces it via preinstall).
3. Start infra (optional): `docker compose up -d postgresql keycloak greenmail`.
4. Backend build & run: `./gradlew :server:engine:bootRun` or `./gradlew build` then `java -jar ...`.
5. Frontend dev (example): `pnpm --filter @cvix/webapp dev` or run `pnpm run dev` to start all
   configured dev servers.

Commands (copyable):

```bash
pnpm install
docker compose up -d
./gradlew build
./gradlew :server:engine:bootRun
pnpm --filter @cvix/webapp dev
```

## Developer workflows

- Pre-commit hooks are installed by `lefthook` (root `prepare` script). Hooks run Biome, changed-files summaries and other lightweight checks.
- Pre-push runs heavier checks: link checking, Detekt, tests and builds.
- Use `pnpm run dev` at the repo root to run frontend dev servers in parallel and start the backend if configured.

## Testing & quality gates

- Unit and integration tests live with their respective modules. The backend uses JUnit/Testcontainers and Gradle test tasks.
- Static analysis: Detekt (Kotlin) and Biome (JS). A `detektAll` task and project-level Biome config enforce style rules.
- Secrets scanning and OWASP dependency checks are wired into CI.

## CI/CD and release notes

- GitHub Actions implement CI pipelines for backend and frontend, build images, run security scans (Trivy), and publish artifacts.
- Gradle is configured to produce boot images (`bootBuildImage`) in CI when building Docker artifacts.

## Observability & runtime features

- Spring Boot Actuator endpoints are available for health, metrics and Prometheus scraping.
- Generated API docs (Spring REST Docs) are available under `build/generated-snippets` when the backend tests run.

## Security and environment

- Keycloak is included for authentication flows and can be run locally from `infra/keycloak`.
- Local TLS certs and helper scripts are provided under `ssl/` and `infra/` for development.

## Assumptions made while producing this overview

- The repository uses the Gradle/Kotlin and pnpm workspace configuration visible in the project root (`build.gradle.kts`, `package.json`, `compose.yaml`).
- Module-specific runtime settings, ports, and environment variables are defined in each service's `application.yml` or in `infra/` compose files; check those files for exact values.

## Recommended next steps for adopters

1. Update `README.md` with your product name and specific env requirements.
2. Centralize environment variable docs (example `.env.example`) and service ports.
3. If you plan to deploy to a cloud provider, add infrastructure as code (Terraform/Bicep) and document secrets management.
4. Add or customize example data and Postgres initialization scripts in `infra/postgresql/init-scripts` for easier demos.

## Requirements coverage

- Produce a clear product overview: Done (this document).
- Analyze project layout, tech stack and workflows: Done.
- Provide quickstart and commands: Done.
- Provide assumptions and next steps: Done.

---

For detailed, module-level docs (APIs, DB schemas, Keycloak realms), I can generate per-module READMEs or extract configuration snippets — tell me which module to prioritize next.
