# Contributing to cvix

Thanks for your interest! This guide summarizes how to set up the environment, run tests, and open high-quality PRs.

## Requirements

- Java 21 (Temurin)
- Node.js 20 and pnpm 9
- Docker (for Postgres/Testcontainers)
- Git

## Quick setup

```bash
pnpm install
./gradlew --version
```

### Backend

```bash
./gradlew build
```

### Frontend

```bash
pnpm -r run lint --if-present
pnpm -r run test --if-present
pnpm -r run build --if-present
```

### Database

- Use `compose.yaml` to run PostgreSQL locally if needed.
- Migrations in `src/main/resources/db/changelog/`.

## Tests

- Backend: JUnit 5 + Testcontainers.
- Frontend: Vitest + Vue Test Utils.
- Naming: `should_doSomething_when_condition`.

## Principles

- "We grow only if it improves the experience."
- Security first: input validation, RBAC, minimal dependency surface.
- Small, focused, well-described PRs.

## Commits

Use Conventional Commits:

- feat, fix, docs, chore, refactor, test, perf, build, ci

## Opening a PR

1. Create a branch from `main`.
2. Follow the PR template.
3. Link an issue ("Closes #123").
4. Ensure CI passes (Gradle and pnpm).
5. Update documentation in `docs/src/content/docs` if applicable.
6. Request review (CODEOWNERS will be auto-requested).

## Code standards

- Kotlin: 4 spaces, KDoc for public APIs.
- Vue/TS: Composition API, strict typing, JSDoc for complex functions.
- Lint/format must pass.

## Security

- Do not commit secrets. Use environment variables/GitHub Secrets.
- Report vulnerabilities following SECURITY.md.

Thanks for contributing 💚
