# AGENTS.md - AI Coding Agent Guide

> Monorepo: Kotlin/Spring Boot backend + Vue.js/Astro frontend
> Package Manager: pnpm (frontend) + Gradle (backend)

## Essential Commands

```bash
make install          # Install frontend deps
make dev              # Run all frontend apps
make dev-web          # Run webapp only
./gradlew bootRun     # Run backend
make build            # Build all
make verify-all       # Full CI verification
```

## Testing - Single File Execution

### Frontend (Vitest + Playwright)

```bash
# Single test file
pnpm --filter @cvix/webapp vitest run src/path/to/file.spec.ts
pnpm --filter @cvix/utilities vitest run src/chunk/chunk.spec.ts

# Watch mode
pnpm --filter @cvix/webapp vitest src/path/to/file.spec.ts

# E2E
pnpm test:e2e                     # All E2E
pnpm test:e2e:headed              # With browser
```

### Backend (JUnit 5 + Kotest)

```bash
# Single test class
./gradlew test --tests "com.cvix.authentication.domain.RoleTest"

# Single test method (pattern)
./gradlew test --tests "com.cvix.authentication.domain.RoleTest.should*"

# By tag
./gradlew test -PincludeTags=unit
./gradlew test -PincludeTags=integration
```

## Linting

```bash
make lint             # Biome + OxLint (frontend)
make check            # TypeScript + lint
./gradlew detektAll   # Detekt (backend)
```

## Code Style

### TypeScript (Biome)

- Tab indentation, double quotes
- Auto-organized imports
- **No `any`** - use proper types
- Self-closing tags required
- No unused variables/imports
- No parameter reassignment

```typescript
// Good
import type { User } from "@/types";
const getName = (user: User): string => user.name;

// Bad - missing 'type', using 'any'
import { User } from "@/types";
const getName = (user: any) => user.name;
```

### Kotlin (Detekt)

- Max line: 120 chars, indent: 4 spaces
- No wildcard imports (except `java.util.*`, `io.mockk.*`)
- Trailing comma on call sites
- Named args for 3+ parameters
- Boolean prefix: `is`, `has`, `are`

```kotlin
// Good
fun createUser(
    name: String,
    isActive: Boolean = true,
) = User(name = name, isActive = isActive)

// Bad - no trailing comma, wrong boolean name
fun createUser(name: String, active: Boolean)
```

### Naming Conventions

| Element            | TypeScript        | Kotlin            |
|--------------------|-------------------|-------------------|
| Classes/Components | `PascalCase`      | `PascalCase`      |
| Functions          | `camelCase`       | `camelCase`       |
| Constants          | `SCREAMING_SNAKE` | `SCREAMING_SNAKE` |
| Test files         | `*.spec.ts`       | `*Test.kt`        |

## Testing Patterns

### Backend - Use Annotations

```kotlin
@UnitTest
class UserServiceTest {
    @Test
    fun `should create user with valid data`() { ... }
}

@IntegrationTest
class UserControllerIntegrationTest { ... }
```

### Frontend - Vitest + Testing Library

```typescript
describe("UserService", () => {
    it("should fetch user by id", async () => {
        const user = await userService.getById("123");
        expect(user.name).toBe("John");
    });
});
```

## Error Handling

- **Kotlin**: Use `Result<T>`, sealed classes, never swallow exceptions
- **TypeScript**: Use `try/catch` with typed errors, `vue-sonner` for UI

## Commits (Conventional)

```markdown
<type>(<scope>): <description>   # max 120 chars
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

## Project Structure

```markdown
cvix/
├── client/
│   ├── apps/{webapp,marketing,blog}/
│   └── packages/{ui,utilities,i18n}/
├── server/engine/               # Spring Boot
├── shared/{common,spring-boot-common}/
├── infra/                       # Docker
└── specs/                       # Feature specs
```

## Pre-commit Hooks (Lefthook)

- `biome check` on staged files
- `markdownlint` on `.md/.mdx`
- `commitlint` on commit messages
- `detektAll` + `check-secrets.sh` on pre-push

## Key Stack

- **Frontend**: Vue 3.5, Vite, TailwindCSS 4, Pinia, Zod, Vitest, Playwright
- **Backend**: Spring Boot 3.5, Kotlin 2.2, WebFlux, R2DBC, PostgreSQL
- **Testing**: JUnit 5, Kotest, Testcontainers, MockK

## Available Skills

Skills provide detailed patterns and conventions for specific technologies. Located in
`.ruler/skills/`.

| Skill                                                                   | Description                                               | Trigger                                     |
|-------------------------------------------------------------------------|-----------------------------------------------------------|---------------------------------------------|
| [astro](.ruler/skills/astro/SKILL.md)                                   | Astro framework, island architecture, content collections | `.astro` files, content collections         |
| [vue](.ruler/skills/vue/SKILL.md)                                       | Vue 3 Composition API, Pinia, Vee-Validate + Zod          | `.vue` files, composables, stores           |
| [kotlin](.ruler/skills/kotlin/SKILL.md)                                 | Kotlin conventions, coroutines, null safety               | `.kt` files, Kotlin patterns                |
| [spring-boot](.ruler/skills/spring-boot/SKILL.md)                       | Spring Boot WebFlux, R2DBC, security                      | Controllers, services, repositories         |
| [hexagonal-architecture](.ruler/skills/hexagonal-architecture/SKILL.md) | Hexagonal Architecture, Ports & Adapters, CQRS            | Feature structure, domain models, use cases |
| [doc-guardian](.ruler/skills/doc-guardian/SKILL.md)                     | Documentation maintenance, verification                   | Docs updates, API documentation             |
| [playwright](.ruler/skills/playwright/SKILL.md)                         | E2E testing with Playwright, Page Objects                 | E2E tests, browser automation               |
| [tailwind-4](.ruler/skills/tailwind-4/SKILL.md)                         | Tailwind CSS 4 patterns and best practices                | Styling, CSS classes                        |
| [typescript](.ruler/skills/typescript/SKILL.md)                         | TypeScript strict patterns, generics, types               | `.ts` files, type definitions               |
| [zod-4](.ruler/skills/zod-4/SKILL.md)                                   | Zod 4 schema validation, breaking changes from v3         | Form validation, schema definitions         |

### Using Skills

Skills are automatically loaded when working with relevant files. For explicit loading:

```bash
# Verify documentation
.ruler/skills/doc-guardian/assets/verify-docs.sh

# Use a template
cp .ruler/skills/doc-guardian/assets/feature-doc.md \
   client/apps/docs/src/content/docs/features/my-feature.md
```

### Documentation Templates

Located in `.ruler/skills/doc-guardian/assets/`:

- `feature-doc.md` - New feature documentation
- `api-doc.md` - REST API endpoint documentation
- `migration-guide.md` - Breaking changes and migration steps
- `architecture-decision.md` - Architecture Decision Record (ADR)
