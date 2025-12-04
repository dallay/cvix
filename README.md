# ProFileTailors

![ProFileTailors Logo](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/th5xamgrr6se0x5ro4g6.png)

A production-ready SaaS starter template and monorepo for building subscription web apps (backend: Spring Boot + Kotlin, frontend: Vite/Astro/Vue).

## Badges

[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![pnpm](https://img.shields.io/badge/package--manager-pnpm-blue)](https://pnpm.io/)
[![Build](https://img.shields.io/badge/build-gradle-brightgreen)](https://gradle.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-%E2%9C%93-7f52ff?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-%E2%9C%93-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-41B883?logo=vue.js&logoColor=white)](https://vuejs.org/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-4.x-06B6D4?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-%E2%9C%93-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Keycloak](https://img.shields.io/badge/Keycloak-%E2%9C%93-A82A2A?logo=keycloak&logoColor=white)](https://www.keycloak.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-%E2%9C%93-F0A500?logo=liquibase&logoColor=white)](https://www.liquibase.org/)
[![Docker](https://img.shields.io/badge/Docker-%E2%9C%93-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

## Quick overview

- Monorepo: frontend apps in `client/`, backend services in `server/`, shared Kotlin libs in `shared/`.
- Backend: Spring Boot (Kotlin), R2DBC, Liquibase, Testcontainers.
- Frontend: Vite + Vue 3, Astro landing, Tailwind CSS, PNPM workspaces.

## Quickstart (developer)

Requirements: JDK 21+, pnpm >= 10, Docker & Docker Compose, Git

Install JS deps and build:

```bash
make install
```

Start local infra (optional):

```bash
docker compose up -d postgresql keycloak greenmail
```

Run backend (development):

```bash
make backend-run
```

Run frontend dev (example):

```bash
make dev-web
```

Run all tests (frontend + backend):

```bash
make test-all
```

## Available Commands

This project uses a `Makefile` to streamline common development tasks. Below is a list of the main commands (28 targets) and what they actually invoke in the repository:

| Command                        | Description                                                                   |
| ------------------------------ | ----------------------------------------------------------------------------- |
| `make install`                 | Install JavaScript workspace dependencies (`pnpm install`).                   |
| `make update-deps`             | Update JS dependencies to their latest versions via pnpm scripts.             |
| `make prepare`                 | Prepare the development environment (runs `pnpm prepare`).                    |
| `make ruler-check`             | Check the project's architecture rules (`pnpm ruler:check`).                  |
| `make ruler-apply`             | Apply the project's architecture rules (`pnpm ruler:apply`).                  |
| `make dev`                     | Run the landing page in development mode (root `pnpm dev` / landing only).    |
| `make dev-landing`             | Run only the landing page dev server (`pnpm --filter @cvix/marketing dev`).   |
| `make dev-web`                 | Run only the webapp dev server (`pnpm --filter @cvix/webapp dev`).            |
| `make dev-docs`                | Run only the documentation dev server (`pnpm --filter @cvix/docs dev`).       |
| `make build`                   | Build the landing page and backend (`pnpm build`, then `make backend-build`). |
| `make build-landing`           | Build only the landing page (`pnpm --filter @cvix/marketing build`).          |
| `make preview-landing`         | Preview the landing page (`pnpm --filter @cvix/marketing preview`).           |
| `make build-web`               | Build only the web application (`pnpm --filter @cvix/webapp build`).          |
| `make build-docs`              | Build only the documentation site (`pnpm --filter @cvix/docs build`).         |
| `make test`                    | Run frontend tests (root `pnpm test`) — commonly configured to run UI tests.  |
| `make test-ui`                 | Run UI tests (`pnpm test:ui`).                                                |
| `make test-coverage`           | Run all tests with coverage reporting (`pnpm test:coverage`).                 |
| `make lint`                    | Run normal Biome lint (`pnpm lint`).                                          |
| `make lint-strict`             | Run Biome lint in strict mode (`pnpm lint:strict`).                           |
| `make check`                   | Run project checks (`pnpm check`).                                            |
| `make clean`                   | Clean JS build artifacts (`pnpm clean`).                                      |
| `make backend-build`           | Build the backend service (`./gradlew build`).                                |
| `make backend-run`             | Run the backend application (`./gradlew bootRun`).                            |
| `make backend-test`            | Run backend tests (`./gradlew test`).                                         |
| `make backend-clean`           | Clean backend build artifacts (`./gradlew clean`).                            |
| `make cleanup-test-containers` | Clean up Testcontainers left running after tests.                             |
| `make start`                   | Start configured PNPM start script for apps (`pnpm start`).                   |
| `make test-all`                | Run all tests for all applications (`pnpm test:all`).                         |
| `make precommit`               | Run pre-commit checks (`pnpm precommit`).                                     |

## Project structure (high level)

See these folders at the repository root:

- `client/` — frontend apps and shared TS packages
- `server/` — Kotlin/Spring Boot services
- `shared/` — shared Kotlin libraries
- `infra/` — docker compose and helper scripts
- `specs/` — feature specifications and planning documents
- `docs/` — project documentation

### Additional Documentation

- **[Docker Deployment Guide](docs/DOCKER_DEPLOYMENT.md)** — Complete guide for building and deploying Docker images to GHCR
- **[Contributing Guide](CONTRIBUTING.md)** — Development workflow and contribution guidelines
- **[Security Policy](SECURITY.md)** — Security practices and vulnerability reporting

## Release & Versioning

This project uses **Semantic Versioning** with automated releases powered by [semantic-release](https://semantic-release.gitbook.io/).

### How It Works

1. **Conventional Commits** trigger version bumps:
   - `feat:` → Minor version (1.0.0 → 1.1.0)
   - `fix:` → Patch version (1.0.0 → 1.0.1)
   - `feat!:` or `BREAKING CHANGE:` → Major version (1.0.0 → 2.0.0)

2. **Automatic on merge to `main`**:
   - Version calculated from commit history
   - `CHANGELOG.md` generated
   - Git tag created
   - GitHub Release published
   - Docker images pushed to registries

### Docker Images

Images are published to both **GitHub Container Registry** and **Docker Hub**:

```bash
# Pull from GHCR
docker pull ghcr.io/dallay/cvix-engine:latest
docker pull ghcr.io/dallay/cvix-webapp:latest
docker pull ghcr.io/dallay/cvix-marketing:latest

# Pull specific version
docker pull ghcr.io/dallay/cvix-engine:1.2.3
```
```bash
# Pull from Docker Hub
docker pull docker.io/dallay/cvix-engine:latest
docker pull docker.io/dallay/cvix-webapp:latest
docker pull docker.io/dallay/cvix-marketing:latest

# Pull specific version
docker pull docker.io/dallay/cvix-engine:1.2.3
```

**Available Tags (GHCR & Docker Hub):**
| Tag      | Description                                  |
| -------- | -------------------------------------------- |
| `latest` | Rolling tag, always points to newest release |
| `x.y.z`  | Semantic version (e.g., `1.2.3`)             |
| `vX`     | Major version (e.g., `v1`)                   |
| `<sha>`  | Git commit SHA (immutable, GHCR only)        |

### Manual Release (dry-run)

Test what version would be released without actually releasing:

```bash
pnpm release:dry-run
```

## Development Guidelines

### Testing with Testcontainers

The backend integration tests use [Testcontainers](https://testcontainers.com/) to spin up real PostgreSQL, Keycloak, and GreenMail containers.

**Local Development Setup:**

For faster test execution during development, you can enable container reuse by creating/editing `~/.testcontainers.properties`:

```properties
testcontainers.reuse.enable=true
```

With this setting, containers will persist between test runs, speeding up subsequent executions significantly.

**Managing Test Containers:**

When using container reuse, test containers may remain running after tests complete. To clean them up:

```bash
# Clean up all test containers
make cleanup-test-containers

# Or manually
docker rm -f keycloak-tests greenmail-tests
```

**Important Notes:**

- Container reuse is disabled in the code to ensure clean CI/CD builds
- The local `~/.testcontainers.properties` config only affects your development environment
- CI pipelines will always start fresh containers for each test run

## Features

### Resume Generator MVP

A professional resume generation system that converts user-submitted form data into beautifully formatted PDF resumes.

**Key Capabilities:**

- 📝 **Web-Based Form**: Intuitive Vue.js form for entering resume data (personal info, work experience, education, skills)
- 🎨 **Professional LaTeX Templates**: Adaptive templates that adjust layout based on your experience level
- 🌐 **Bilingual Support**: Generate resumes in English or Spanish
- 📱 **Mobile-Friendly**: Responsive design works on desktop and mobile browsers
- ⚡ **Fast Generation**: PDF ready in under 8 seconds (p95)
- 🔒 **Secure**: LaTeX injection protection, Docker container isolation, and rate limiting
- ♿ **Accessible**: WCAG 2.1 AA compliant forms and controls

**Tech Stack:**

- Frontend: Vue 3 + TypeScript, Vee-Validate + Zod, Tailwind CSS
- Backend: Spring Boot (Kotlin), WebFlux (reactive)
- PDF Engine: LaTeX (TeX Live) running in isolated Docker containers
- Template Engine: StringTemplate 4

**Getting Started:**

Prerequisites: Docker daemon must be running and TeX Live image available.

```bash
# Pull TeX Live Docker image (one-time setup)
docker pull texlive/texlive:TL2024-historic

# Start the backend (includes resume API)
make backend-run

# Start the frontend
make dev-web

# Navigate to http://localhost:5173/resume
```

**API Endpoint:**

```bash
POST /api/resume
Content-Type: application/json
Authorization: Bearer <jwt-token>
Accept-Language: en

# See specs/003-resume-generator-mvp/examples/ for sample payloads
```

**Monitoring & SLOs:**

- API Latency: p95 ≤ 200ms (excluding PDF generation)
- PDF Generation: p95 < 8 seconds
- Error Rate: < 3%
- Uptime: 99.5%

View metrics: `/actuator/prometheus`
Health check: `/actuator/health`
Grafana dashboard: `infra/grafana/dashboards/resume-generator-sla.json`

**Browser Compatibility:**

The Resume Generator is tested and supported on the following browsers (last 2 major versions):

| Browser          | Desktop | Mobile | Notes                  |
| ---------------- | ------- | ------ | ---------------------- |
| Chrome/Edge      | ✅       | ✅      | Chromium 120+          |
| Firefox          | ✅       | ✅      | Firefox 121+           |
| Safari           | ✅       | ✅      | Safari 17+ (macOS/iOS) |
| Chrome Mobile    | N/A     | ✅      | Android 13+            |
| Samsung Internet | N/A     | ✅      | Version 23+            |

**Minimum Requirements:**

- ES2020+ JavaScript support
- CSS Grid and Flexbox
- Fetch API
- LocalStorage/SessionStorage
- Modern input types (email, tel, url, date)

**Not Supported:**

- Internet Explorer (all versions)
- Legacy Edge (pre-Chromium)
- Opera Mini (limited JavaScript)
- UC Browser (limited support)

**Accessibility:**

- WCAG 2.1 AA compliant
- Screen reader tested (NVDA, VoiceOver, TalkBack)
- Keyboard navigation fully supported
- Touch targets meet WCAG guidelines (44x44px minimum)

For detailed accessibility information, see: `client/apps/webapp/src/resume/docs/ACCESSIBILITY.md`

**Documentation:**

- Feature Spec: `specs/003-resume-generator-mvp/spec.md`
- Implementation Plan: `specs/003-resume-generator-mvp/plan.md`
- Quickstart Guide: `specs/003-resume-generator-mvp/quickstart.md`
- API Contract: `specs/003-resume-generator-mvp/contracts/resume-api.yaml`
- Monitoring Guide: `specs/003-resume-generator-mvp/monitoring.md`

---

## Contributing

We follow Conventional Commits. See `CONTRIBUTING.md` and the `.ruler/` docs for repo conventions. Pre-commit hooks are installed by `lefthook` in the `prepare` script.

If you open a PR, ensure the CI passes (lint, tests, detekt) and keep PRs small and focused.

## License

This project is licensed under the MIT License — see the `LICENSE` file for details.

## Authors

- [@yacosta738](https://www.github.com/yacosta738) (repo owner and maintainer)

## Further reading

- For module-level docs and quickstarts see the `docs/src/content/docs` and `.ruler/` directories.
