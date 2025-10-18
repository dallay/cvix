# Documentation Guidelines

> This document is the single source of truth for how project documentation and development guidelines are maintained.

## Canonical Source

All project documentation, architectural decisions, and development conventions are maintained within the `.ruler/` directory. This serves as the single source of truth for both human developers and AI assistants.

## Structure

- **Logical Grouping**: Rules are organized into subdirectories by category (e.g., `00_GENERAL`, `01_BACKEND`, `02_FRONTEND`).
- **File per Topic**: Each Markdown file should cover a single, focused topic (e.g., `KOTLIN_CONVENTIONS.md`).
- **Standard Format**: Every file must start with a clear title and a blockquote summarizing its purpose.

## Agent Instructions

- **Primacy of `.ruler`**: AI assistants must always prefer rules found in `.ruler/` over repository-level heuristics or older documentation.
- **Adherence to Conventions**: When making code changes, strictly follow the project's conventions for linting, testing, and running validation commands (`./gradlew check`, `pnpm check`).
- **Security First**: For any security-related decisions, adhere to `04_DEVOPS/02_SECURITY_PRACTICES.md`. Prioritize least-privilege, parameterized queries, and secure secret management.

## Maintenance

- To update any convention or guideline, modify the relevant file within the `.ruler/` directory.
- The `instructions.md` file you are reading now is generated from this file and should not be edited directly.
