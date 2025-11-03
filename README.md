# Loomify

![Loomify Logo](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/th5xamgrr6se0x5ro4g6.png)

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

| Command                | Description                                                                    |
| ---------------------- | ------------------------------------------------------------------------------ |
| `make install`         | Install JavaScript workspace dependencies (`pnpm install`).                    |
| `make update-deps`     | Update JS dependencies to their latest versions via pnpm scripts.              |
| `make prepare`         | Prepare the development environment (runs `pnpm prepare`).                     |
| `make ruler-check`     | Check the project's architecture rules (`pnpm ruler:check`).                   |
| `make ruler-apply`     | Apply the project's architecture rules (`pnpm ruler:apply`).                   |
| `make dev`             | Run the landing page in development mode (root `pnpm dev` / landing only).     |
| `make dev-landing`     | Run only the landing page dev server (`pnpm --filter @loomify/marketing dev`). |
| `make dev-web`         | Run only the webapp dev server (`pnpm --filter @loomify/webapp dev`).          |
| `make dev-docs`        | Run only the documentation dev server (`pnpm --filter @loomify/docs dev`).     |
| `make build`           | Build the landing page and backend (`pnpm build`, then `make backend-build`).  |
| `make build-landing`   | Build only the landing page (`pnpm --filter @loomify/marketing build`).        |
| `make preview-landing` | Preview the landing page (`pnpm --filter @loomify/marketing preview`).         |
| `make build-web`       | Build only the web application (`pnpm --filter @loomify/webapp build`).        |
| `make build-docs`      | Build only the documentation site (`pnpm --filter @loomify/docs build`).       |
| `make test`            | Run frontend tests (root `pnpm test`) — commonly configured to run UI tests.   |
| `make test-ui`         | Run UI tests (`pnpm test:ui`).                                                 |
| `make test-coverage`   | Run all tests with coverage reporting (`pnpm test:coverage`).                  |
| `make lint`            | Run normal Biome lint (`pnpm lint`).                                           |
| `make lint-strict`     | Run Biome lint in strict mode (`pnpm lint:strict`).                            |
| `make check`           | Run project checks (`pnpm check`).                                             |
| `make clean`           | Clean JS build artifacts (`pnpm clean`).                                       |
| `make backend-build`   | Build the backend service (`./gradlew build`).                                 |
| `make backend-run`     | Run the backend application (`./gradlew bootRun`).                             |
| `make backend-test`    | Run backend tests (`./gradlew test`).                                          |
| `make backend-clean`   | Clean backend build artifacts (`./gradlew clean`).                             |
| `make start`           | Start configured PNPM start script for apps (`pnpm start`).                    |
| `make test-all`        | Run all tests for all applications (`pnpm test:all`).                          |
| `make precommit`       | Run pre-commit checks (`pnpm precommit`).                                      |

## Project structure (high level)

See these folders at the repository root:

- `client/` — frontend apps and shared TS packages
- `server/` — Kotlin/Spring Boot services
- `shared/` — shared Kotlin libraries
- `infra/` — docker compose and helper scripts

## Contributing

We follow Conventional Commits. See `CONTRIBUTING.md` and the `.ruler/` docs for repo conventions. Pre-commit hooks are installed by `lefthook` in the `prepare` script.

If you open a PR, ensure the CI passes (lint, tests, detekt) and keep PRs small and focused.

## License

This project is licensed under the MIT License — see the `LICENSE` file for details.

## Authors

- [@yacosta738](https://www.github.com/yacosta738) (repo owner and maintainer)

## Further reading

- For module-level docs and quickstarts see the `docs/src/content/docs` and `.ruler/` directories.
