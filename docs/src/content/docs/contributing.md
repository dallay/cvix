---
title: "Contributing"
description: "Guidelines for contributing to the Loomify project and documentation."
---

## Contributing to Loomify

Thank you for considering contributing to Loomify! This page explains how to report issues, propose changes, and contribute code or documentation.

## Ways to contribute

- Fix bugs or implement small features.
- Improve or add documentation pages.
- Suggest design or architecture improvements.
- Help review pull requests and triage issues.

## Reporting bugs and requesting features

- Search existing issues first to avoid duplicates.
- When filing a bug, include:
  - A clear summary and steps to reproduce.
  - Expected vs actual behavior.
  - Relevant logs or error messages.
  - Platform details (OS, Java/Node versions) when relevant.
- For feature requests, explain the problem, the desired behavior, and any alternatives.

## Working on code

1. Create a branch from `main` with a descriptive name, e.g. `fix/cleanup-config` or `feat/add-health-check`.
2. Keep changes focused and small — a single logical change per PR.
3. Run tests and linters locally before opening a PR.

### Running tests & checks (examples)

- JVM/Gradle (backend):

```bash
./gradlew :server:engine:test
```

- JS/TS (frontend/docs):

```bash
pnpm --filter client... test
```

- Run detekt (Kotlin lint):

```bash
./gradlew detektAll -x test
```

## Documentation contributions

- Docs live under `docs/src/content/docs/`.
- Each doc file must include YAML frontmatter with at least a `title` field. Example:

```md
---
title: "Quick Start"
description: "How to run Loomify locally"
---
```

- Use Starlight components and patterns when creating interactive or rich pages. See the Starlight docs: [starlight.astro.build](https://starlight.astro.build/)
- Keep documentation updates in the same PR as code changes when they are directly related.

## PR checklist

Before requesting a review, ensure:

- Code builds and tests pass locally.
- Linting rules pass (`detekt`, Biome if applicable).
- Documentation updated if the change affects behavior or public APIs.
- The PR description explains the reason and the change; link related issues.

## Reviewing

- Reviewers should focus on behavior, tests, and clarity of the change.
- Suggest small, actionable improvements in review comments.

## Style and conventions

- Follow existing code style and naming conventions.
- Keep changes backwards-compatible when possible; document breaking changes in the changelog.

## License and CLA

By contributing you agree that your contributions will be licensed under the project's existing license (see root `LICENSE`). If your project requires a Contributor License Agreement (CLA), it will be documented in the repository.

## Code of Conduct

Please follow the project's Code of Conduct in `CODE_OF_CONDUCT.md` when interacting in issues and PRs.

---

Thank you — your contributions make Loomify better for everyone. If you want, I can add a PR template or a small CONTRIBUTING checklist file at the repo root.
