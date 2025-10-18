---
title: "Developer Guide"
description: "Guidance for contributors and developers working on the repo."
---

## Developer Guide

This section covers code organization, conventions, and common workflows.

## Branches and commits

- Follow your team's branching strategy (Gitflow or similar).
- Keep commits small, focused, and descriptive.

## Relevant structure

- `server/engine` — domain logic and services.
- `shared/` — shared libraries used across services and clients.
- `client/packages` — frontend utilities and packages.

## Adding a new package

1. Create a new module under `shared/` or `client/packages`.
2. Define its `build.gradle.kts` (or package.json for JS packages).
3. Update `settings.gradle.kts` or workspace config if necessary.

## Running local tests (examples)

- JVM tests (Gradle):

```bash
./gradlew :server:engine:test --tests "*CreateWorkspaceCommandHandlerTest"
```

- Frontend tests (Vitest):

```bash
pnpm --filter client... test
```

## Code quality

- Review `detekt.yml` and run `./gradlew detektAll -x test` before sending PRs.

---

Add more sections (architecture, CI/CD, release process) as needed for your team.
