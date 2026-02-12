# Contributing to cvix

Thanks for your interest! This guide summarizes how to set up the environment, run tests, and open high-quality PRs.

## Requirements

- Java 21 (Temurin)
- Node.js 24.12.0 and pnpm 10+
- Docker (for Postgres/Testcontainers)
- Git

## Quick setup

The project uses a centralized `Makefile` to simplify development.

```bash
make prepare-env   # Setup .env and config files
make install       # Install Node & Gradle dependencies
make ssl-cert      # Generate dev SSL certs
```

### Backend

```bash
make backend-build  # Build backend
make backend-run    # Run backend services (Postgres, Keycloak) and application
```

### Frontend

```bash
make lint           # Frontend linting
make test           # Frontend unit tests
make build          # Build all frontend apps
```

### Verification

Always run this command before opening a PR:

```bash
make verify-all     # Full project verification (lint, test, build)
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

Use [Conventional Commits](https://www.conventionalcommits.org/):

- `feat`: New feature (triggers MINOR version bump)
- `fix`: Bug fix (triggers PATCH version bump)
- `docs`: Documentation only changes
- `chore`: Maintenance tasks
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `test`: Adding or updating tests
- `perf`: Performance improvement
- `build`: Build system or dependency changes
- `ci`: CI/CD configuration changes

### Breaking Changes

To trigger a MAJOR version bump, include `BREAKING CHANGE:` in the commit footer or use `!` after the type:

```bash
feat!: remove deprecated API endpoint

BREAKING CHANGE: The /api/v1/legacy endpoint has been removed.
```

## Versioning Strategy

This project uses **Semantic Versioning** with automated releases:

- **Unified versioning**: All components (frontend and backend) share the same version number
- **Automatic version bumping**: Based on conventional commits
- **Zero manual version editing**: The release workflow handles everything

### Version Files

- `gradle.properties`: Backend version (`version = x.y.z`)
- `package.json`: Root package version
- `client/**/package.json`: Frontend workspace package versions

### Release Flow

1. Merge PR to `main` with conventional commits
2. Semantic Release analyzes commits
3. If releasable changes are detected:
   - Version is calculated (major/minor/patch)
   - `CHANGELOG.md` is updated
   - Version files are updated
   - Git tag is created (e.g., `v1.2.3`)
   - GitHub Release is published
   - Docker images are built and pushed

### Docker Images

Images are published to multiple registries with semantic version tags:

- **GHCR**: `ghcr.io/dallay/cvix-*`
- **Docker Hub**: `docker.io/<username>/cvix-*`

Tags: `latest`, `<version>`, `v<major>`, `<sha>`

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
