# Contributing to ProFileTailors

Thanks for your interest! This guide summarizes how to set up the environment, run tests, and open high-quality PRs.

## Requirements

- Java 21 (Temurin) or higher
- Node.js 22 and pnpm 10 or higher
- Docker (for Postgres/Testcontainers)
- Make utility
- Git

## Quick setup

The project uses a centralized `Makefile` to simplify common tasks.

```bash
make prepare-env   # Setup .env and config files
make install       # Install all dependencies
make ssl-cert      # Generate local SSL certificates
```

### Backend

```bash
make backend-build
```

### Frontend

```bash
make lint
make test
make build
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
4. **Ensure all checks pass** by running the full verification suite:
   ```bash
   make verify-all
   ```
5. Update documentation in `docs/src/content/docs` if applicable.
6. Request review (CODEOWNERS will be auto-requested).

## Documentation Contributions

Documentation is a first-class citizen in ProFileTailors. If you are contributing documentation:

- Public-facing docs live in `docs/src/content/docs/`.
- Use `make dev-docs` to preview your changes locally.
- Ensure all code examples are syntax-highlighted (`typescript`, `kotlin`, `bash`).
- Follow the structure and tone of existing documentation.

## Code standards

- Kotlin: 4 spaces, KDoc for public APIs.
- Vue/TS: Composition API, strict typing, JSDoc for complex functions.
- Lint/format must pass.

## Security

- Do not commit secrets. Use environment variables/GitHub Secrets.
- Report vulnerabilities following SECURITY.md.

Thanks for contributing 💚
