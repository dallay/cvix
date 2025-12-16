# General Guidelines

> Core conventions that apply across the entire codebase: product context, Git workflow, code review, and documentation standards.

## Product Overview

### ProFile Tailors (cvix)

The Resume Generator is a streamlined platform that empowers users to craft polished, professional résumés through a guided, intuitive experience. Instead of fighting with complex editors or rigid templates, users simply provide their information once and select a visual style that fits their personal brand. The system automatically assembles the content into a clean, structured layout while maintaining consistency, readability, and modern design standards.

The platform focuses on speed, quality, and reliability: users can create multiple résumé versions tailored to different job applications, maintain a living profile with their career history, and export final documents in high-fidelity formats. Templates follow proven best practices for hiring funnels and applicant tracking systems, ensuring maximum impact across both human and automated screening.

The result is a simple, elegant tool that turns raw career data into a compelling, professionally crafted résumé—without requiring design skills, formatting knowledge, or technical experience.

---

## Git Conventions

### Branch Naming Strategy

All branch names must follow a prefix-based convention to clearly indicate their purpose.

| Prefix      | Purpose                                                  | Example                         |
|-------------|----------------------------------------------------------|---------------------------------|
| `feature/`  | New features or enhancements                             | `feature/user-authentication`   |
| `fix/`      | Bug fixes                                                | `fix/login-form-validation`     |
| `docs/`     | Documentation-only changes                               | `docs/update-readme`            |
| `chore/`    | Routine maintenance, refactoring, or build-related tasks | `chore/upgrade-gradle-wrapper`  |
| `refactor/` | Code changes that neither fix a bug nor add a feature    | `refactor/extract-user-service` |

### Commit Message Format

We adhere to the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification. This format enables automated changelog generation and semantic versioning.

```text
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

#### Type

Must be one of the following:

| Type       | Description                                                                       |
|------------|-----------------------------------------------------------------------------------|
| `feat`     | A new feature                                                                     |
| `fix`      | A bug fix                                                                         |
| `docs`     | Documentation only changes                                                        |
| `style`    | Changes that do not affect the meaning of the code (white-space, formatting, etc) |
| `refactor` | A code change that neither fixes a bug nor adds a feature                         |
| `perf`     | A code change that improves performance                                           |
| `test`     | Adding missing tests or correcting existing ones                                  |
| `build`    | Changes that affect the build system or external dependencies                     |
| `ci`       | Changes to our CI configuration files and scripts                                 |
| `chore`    | Other changes that don't modify src or test files                                 |

#### Scope (Optional)

The scope provides additional contextual information and is contained within parentheses. E.g., `feat(api): ...`, `fix(client): ...`

#### Description

- Use the imperative, present tense: "change" not "changed" nor "changes"
- Don't capitalize the first letter
- No dot (.) at the end

#### Example Commits

```text
feat(auth): add password reset functionality
```

```text
fix(api): correct pagination query parameter

Closes #123
```

### Commit Emojis

We encourage adding a single, standard Unicode emoji to commit messages to make PRs and changelogs more scannable.

| Type          | Emoji | Example                                             |
|---------------|-------|-----------------------------------------------------|
| feat          | ✨     | `feat(auth): ✨ add password reset functionality`    |
| fix           | 🐛    | `fix(api): 🐛 correct pagination query parameter`   |
| docs          | 📝    | `docs(readme): 📝 update quickstart instructions`   |
| style         | 🎨    | `style(ui): 🎨 tidy CSS and fix spacing`            |
| refactor      | ♻️    | `refactor(core): ♻️ extract user service`           |
| perf          | 🚀    | `perf(cache): 🚀 improve lookup throughput`         |
| test          | 🧪    | `test(api): 🧪 add integration test for pagination` |
| build/deps    | 📦    | `build(deps): 📦 bump vite to ^7.1.0`               |
| ci            | ⚙️    | `ci(actions): ⚙️ add workflow for release`          |
| chore         | 🔧    | `chore: 🔧 update README badges`                    |
| revert/remove | 🔥    | `fix(api): 🔥 remove deprecated endpoint`           |

**Guidelines:**

- Place the emoji after the type/scope prefix
- Use at most one emoji per commit header
- Keep commit headers short (<=72 chars) with the emoji included
- Refer to `commitlint.config.mjs` for the enforced commit message rules

---

## Code Review Guidelines

### The Golden Rule

Treat every code review as a learning opportunity. Provide constructive, respectful, and clear feedback. The goal is to improve the code, not to criticize the author.

### For the Author (Submitting a Pull Request)

1. **Self-Review First**: Before requesting a review, perform a self-review of your own PR. Check for typos, debug code, and ensure it meets all requirements.
2. **Write a Clear PR Description**: Explain:
   - **What** the change is
   - **Why** the change is needed (link to the issue/ticket)
   - **How** the changes were implemented (if complex)
3. **Keep PRs Small and Focused**: A PR should ideally address a single concern. Small PRs are easier and faster to review.
4. **Ensure CI is Green**: All automated checks (tests, linting, builds) must pass before you request a review.

### For the Reviewer

1. **Understand the Context**: Read the PR description and the related issue to understand the purpose of the change.
2. **Provide Constructive Feedback**:
   - Be specific. Instead of "this is confusing," say "Can we rename this variable to `userProfile` for clarity?"
   - Offer suggestions for improvement
   - Use comments to ask clarifying questions
3. **Review for Key Areas**:
   - **Correctness**: Does the code do what it's supposed to do? Does it handle edge cases?
   - **Readability & Maintainability**: Is the code easy to understand? Does it follow our established conventions?
   - **Security**: Does the change introduce any security vulnerabilities (e.g., injection, XSS)?
   - **Performance**: Are there any obvious performance bottlenecks?
   - **Tests**: Are there enough tests? Do they cover the changes effectively?
4. **Approve or Request Changes**:
   - If the PR is good to go, approve it
   - If changes are needed, leave specific comments and select "Request changes"

### Approval Process

- A PR must be approved by at least **one** other team member before it can be merged
- The author is responsible for merging the PR after approval
- Address all comments and resolve conversations before merging

---

## Documentation Guidelines

### Canonical Source

All project documentation, architectural decisions, and development conventions are maintained within the `.ruler/` directory. This serves as the single source of truth for both human developers and AI assistants.

### Structure

- **Logical Grouping**: Rules are organized into subdirectories by category (`backend/`, `frontend/`, `reference/`, `sop/`)
- **File per Topic**: Each Markdown file should cover a single, focused topic
- **Standard Format**: Every file must start with a clear title and a blockquote summarizing its purpose

### Agent Instructions

- **Primacy of `.ruler`**: AI assistants must always prefer rules found in `.ruler/` over repository-level heuristics or older documentation
- **Adherence to Conventions**: When making code changes, strictly follow the project's conventions for linting, testing, and running validation commands (`./gradlew check`, `pnpm check`)
- **Security First**: Prioritize least-privilege, parameterized queries, and secure secret management

### Maintenance

- To update any convention or guideline, modify the relevant file within the `.ruler/` directory
- The generated instruction files (CLAUDE.md, GEMINI.md, etc.) should not be edited directly
