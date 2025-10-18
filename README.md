
![Loomify Logo](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/th5xamgrr6se0x5ro4g6.png)

# Loomify

A production-ready SaaS starter template and monorepo for building subscription web apps (backend: Spring Boot + Kotlin, frontend: Vite/Astro/Vue).

## Badges

[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![pnpm](https://img.shields.io/badge/package--manager-pnpm-blue)](https://pnpm.io/)
[![Build](https://img.shields.io/badge/build-gradle-brightgreen)](https://gradle.org/)

<!-- Language & Tech badges -->
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
pnpm install
```

Start local infra (optional):

```bash
docker compose up -d postgresql keycloak greenmail
```

Run backend (development):

```bash
./gradlew :server:engine:bootRun
```

Run frontend dev (example):

```bash
pnpm --filter @loomify/webapp dev
```

Run all tests (frontend + backend):

```bash
pnpm test
./gradlew test
```

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
