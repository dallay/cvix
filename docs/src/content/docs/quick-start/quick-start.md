---
title: "Quick Start"
description: "Quick guide to get the project running locally."
---

## Quick Start — Quick guide

These steps take you from checkout to running the project in development (summary).

Prerequisites:

- Node.js (v18+ recommended)
- pnpm (or npm/yarn)
- Java 17
- Docker (optional, for services like Postgres/Keycloak)

1. Install dependencies (repo root):

```bash
pnpm install
```

1. Build the server engine (if you're working on the backend):

```bash
./gradlew :server:engine:build -x test
```

1. Start infrastructure services (optional):

- Check the `infra/` folder for docker-compose examples and helper scripts.

1. Run the documentation site locally (Astro):

```bash
cd docs
pnpm install
pnpm dev
```

1. Run the frontend app (simple example):

```bash
cd client/apps/web
pnpm install
pnpm dev
```

Notes:

- To run backend tests use the appropriate Gradle tasks.
- Adjust Java/Node versions according to `gradle.properties` and `package.json` in the repo.
