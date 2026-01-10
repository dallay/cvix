---
title: "Configuration"
description: "Important configuration points and where to change them."
---

## Configuration

This page lists important configuration points and where to change them.

## Variables and properties

- `gradle.properties` — global Gradle properties.
- `package.json` and `pnpm-workspace.yaml` — JS dependencies and workspace config.
- `infra/` — docker-compose files and configuration for local services (Postgres, Keycloak).

## Key files

- `config/detekt.yml` — lint rules for Kotlin.
- `docs/astro.config.mjs` — documentation configuration (Starlight + Mermaid).

## Changing the docs logo

Edit `docs/src/assets/logo.svg` and update `astro.config.mjs` if you change the path.

---

Include environment variable examples and configuration snippets your team needs.
