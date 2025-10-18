# Git Conventions

> This guide outlines the standards for Git, including branch naming and commit message formats, to ensure a clean, navigable, and automated version history.

## Branch Naming Strategy

All branch names must follow a prefix-based convention to clearly indicate their purpose.

- **`feature/<description>`**: For new features or enhancements.
  - Example: `feature/user-authentication`
- **`fix/<description>`**: For bug fixes.
  - Example: `fix/login-form-validation`
- **`docs/<description>`**: For documentation-only changes.
  - Example: `docs/update-readme`
- **`chore/<description>`**: For routine maintenance, refactoring, or build-related tasks.
  - Example: `chore/upgrade-gradle-wrapper`
- **`refactor/<description>`**: For code changes that neither fix a bug nor add a feature.
  - Example: `refactor/extract-user-service`

## Commit Message Format

We adhere to the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification. This format enables automated changelog generation and semantic versioning.

The commit message should be structured as follows:

```text
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Type

Must be one of the following:

- **feat**: A new feature.
- **fix**: A bug fix.
- **docs**: Documentation only changes.
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc).
- **refactor**: A code change that neither fixes a bug nor adds a feature.
- **perf**: A code change that improves performance.
- **test**: Adding missing tests or correcting existing ones.
- **build**: Changes that affect the build system or external dependencies.
- **ci**: Changes to our CI configuration files and scripts.
- **chore**: Other changes that don't modify src or test files.

### Scope (Optional)

The scope provides additional contextual information and is contained within parentheses. E.g., `feat(api): ...`, `fix(client): ...`

### Description

The description contains a concise summary of the change.

- Use the imperative, present tense: "change" not "changed" nor "changes".
- Don't capitalize the first letter.
- No dot (.) at the end.

### Example Commits

```text
feat(auth): add password reset functionality
```

```text
fix(api): correct pagination query parameter

Closes #123
```

## Commit emojis

We encourage adding a single, standard Unicode emoji supported by GitHub and GitLab to commit messages to make PRs and changelogs more scannable. Emojis must be plain Unicode (not images) so both GitHub and GitLab render them correctly.

Guidelines:

- Place the emoji after the type/scope prefix or at the start of the description. Keep messages readable by humans and machines (Conventional Commits must still be parseable).
- Use at most one emoji per commit header.
- Do not replace the Conventional Commit `type(scope): description` contract — the emoji is decorative and optional, not a substitute for the `type`.
- Refer to [commitlint.config.mjs](../../commitlint.config.mjs) for the enforced commit message rules.

Recommended emoji mapping and examples:

- feat: ✨ (sparkles)
  - Example: `feat(auth): ✨ add password reset functionality`
- fix: 🐛 (bug)
  - Example: `fix(api): 🐛 correct pagination query parameter`
- docs: 📝 (memo)
  - Example: `docs(readme): 📝 update quickstart instructions`
- style: 🎨 (art)
  - Example: `style(ui): 🎨 tidy CSS and fix spacing`
- refactor: ♻️ (recycle)
  - Example: `refactor(core): ♻️ extract user service`
- perf: 🚀 (rocket)
  - Example: `perf(cache): 🚀 improve lookup throughput`
- test: 🧪 (white_check_mark)
  - Example: `test(api): 🧪 add integration test for pagination`
- build / deps: 📦 (package) or 🔧 (wrench)
  - Example: `build(deps): 📦 bump vite to ^7.1.0`
- ci: ⚙️ (gear)
  - Example: `ci(actions): ⚙️ add workflow for release`
- chore: 🔧 (wrench)
  - Example: `chore: 🔧 update README badges`
- revert/remove: 🔥 (fire) or ⬅️ (left_arrow)
  - Example: `fix(api): 🔥 remove deprecated endpoint`

Notes:

- Keep commit headers short (<=72 chars) with the emoji included. Use the body for longer explanations.
- CI and tooling that parse Conventional Commits should still see the `type` at the start; prefer `type(scope): emoji description` to preserve parsers that look at the beginning of the header.
- If you are generating changelogs automatically, ensure your changelog tool supports or ignores emojis; most treat them as plain text.
