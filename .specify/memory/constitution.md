# Loomify SaaS Template Constitution

<!--
Sync Impact Report - Version 1.0.0 (Initial Creation)
======================================================
Version Change: none → 1.0.0
Principles Created:
  - I. Hexagonal Architecture (Ports & Adapters)
  - II. Test-Driven Development (NON-NEGOTIABLE)
  - III. Code Quality & Static Analysis
  - IV. Security-First Development
  - V. User Experience Consistency
  - VI. Performance & Scalability
  - VII. Observability & Monitoring
Added Sections:
  - Technology Standards
  - Development Workflow
  - Quality Gates
  - Governance
Templates Status:
  ✅ plan-template.md - Aligned with constitution principles
  ✅ spec-template.md - Includes user story prioritization and acceptance criteria
  ✅ tasks-template.md - Organized by user story with test-first approach
Follow-up TODOs: None
-->

## Core Principles

### I. Hexagonal Architecture (Ports & Adapters)

**All backend features MUST follow Clean Architecture layering:**

- **Domain Layer**: Pure Kotlin business logic with zero framework dependencies. Contains entities, value objects, domain events, exceptions, and repository interfaces (ports).
- **Application Layer**: Framework-agnostic use cases implementing CQRS (commands/queries). Orchestrates domain logic without knowledge of infrastructure details.
- **Infrastructure Layer**: Adapters connecting to external systems (HTTP controllers, R2DBC repositories, external APIs). This is the ONLY layer permitted to use Spring Boot and framework-specific features.

**Dependency Rule**: Dependencies MUST point inward. Domain depends on nothing. Application depends on domain. Infrastructure depends on both but neither depends on infrastructure.

**Rationale**: This architecture ensures testability, maintainability, and the ability to swap implementations (e.g., database, web framework) without touching business logic. It enforces separation of concerns and makes the codebase resilient to framework changes.

### II. Test-Driven Development (NON-NEGOTIABLE)

**TDD is mandatory for all new features:**

1. **Write tests first** → User approves acceptance criteria → Tests fail (red)
2. **Implement minimal code** to make tests pass (green)
3. **Refactor** while keeping tests green

**Test Pyramid MUST be maintained:**

- **Base (largest)**: Unit tests - fast, isolated, testing individual functions/components
- **Middle**: Integration tests - verify component interactions (use Testcontainers for backend, mock API calls for frontend)
- **Top (smallest)**: E2E tests - critical user flows only (Playwright for full system tests)

**Coverage targets:**

- Backend: Minimum 80% code coverage (Kover) focusing on domain and application layers
- Frontend: Minimum 75% coverage (Vitest) for components and composables
- ALL business logic MUST have unit tests
- ALL API contracts MUST have contract tests
- ALL critical user flows MUST have E2E tests

**Test naming**: Use descriptive names following the pattern `should do something when condition` (in backticks for Kotlin tests).

**Rationale**: TDD ensures code is designed for testability from the start, reduces bugs, serves as living documentation, and enables confident refactoring. The test pyramid balances thoroughness with execution speed.

### III. Code Quality & Static Analysis

**Code quality is enforced automatically and MUST pass before merge:**

**Backend (Kotlin):**

- Follow official Kotlin coding conventions
- Use `val` over `var` (immutability by default)
- Strictly AVOID the `!!` operator (null-safety is mandatory)
- Prefer sealed classes/interfaces for restricted hierarchies
- Use `Result<T>` or sealed classes for error handling instead of throwing exceptions
- Run `./gradlew detektAll` - zero violations required
- Use Detekt for static analysis with project-specific rules in `config/detekt.yml`

**Frontend (TypeScript/Vue):**

- Use Biome for all linting and formatting (`pnpm check`)
- TypeScript `strict` mode MUST be enabled
- AVOID `any` - use `unknown` with type guards instead
- Use `<script setup lang="ts">` for all Vue components
- Prefer `type` over `interface` for object shapes
- Use absolute imports with `@/` alias
- Prefer named exports over default exports

**General:**

- Keep functions small and single-purpose (max 50 lines)
- Use descriptive variable names (no single-letter names except loop indices)
- Comment WHY not WHAT (code should be self-documenting)
- All public APIs MUST have documentation comments

**Rationale**: Consistent code style reduces cognitive load, makes code reviews faster, prevents bugs, and ensures codebase maintainability as the team grows.

### IV. Security-First Development

**All code MUST follow OWASP Top 10 best practices:**

**Access Control (A01):**

- ALWAYS enforce authorization on the backend - NEVER trust frontend-only checks
- Apply principle of least privilege by default
- Verify permissions against specific resources on every request

**Cryptography (A02):**

- Use Argon2 or bcrypt for password hashing
- Use TLS 1.2+ for data in transit
- Use AES-GCM for data at rest
- NEVER hardcode secrets - use environment variables or secret management services

**Injection Prevention (A03):**

- ALL database queries MUST use parameterized statements (R2DBC for Kotlin)
- NEVER concatenate user input into queries
- Encode all user-supplied data before rendering in UI
- Use Vue's built-in `{{ }}` templating (auto-escapes)
- When inserting HTML, sanitize with DOMPurify first

**Security Configuration (A05):**

- Set security headers: `Content-Security-Policy`, `Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options`
- Disable verbose error messages and stack traces in production
- Use `HttpOnly`, `Secure`, and `SameSite=Strict` for session cookies

**Dependencies (A06):**

- Run `pnpm audit` and OWASP Dependency-Check in CI
- Update vulnerable dependencies within 48 hours of disclosure
- Pin dependency versions for reproducible builds

**Input Validation:**

- Validate ALL input for type, length, format, and range
- Treat all external data as untrusted (users, APIs, databases)

**Rationale**: Security vulnerabilities can destroy user trust and the business. Building security in from the start is exponentially cheaper than fixing breaches. These practices are industry-standard and prevent the most common attack vectors.

### V. User Experience Consistency

**Frontend MUST provide a cohesive, accessible experience:**

**Design System:**

- Use Shadcn-Vue as the primary UI component library
- Use Tailwind CSS utility classes for styling
- Maintain consistent spacing, typography, and color schemes via design tokens
- Custom components ONLY when Shadcn-Vue cannot fulfill the requirement

**Accessibility (a11y):**

- ALL interactive elements MUST be keyboard accessible
- Use semantic HTML (`<nav>`, `<main>`, `<article>`, `<section>`, `<footer>`)
- Provide descriptive `alt` attributes for images
- Use `aria-*` attributes where necessary
- Test with screen readers and keyboard-only navigation

**Internationalization (i18n):**

- ALL user-facing text MUST use `vue-i18n` (`$t()` function)
- Organize translation keys by domain (e.g., `userProfile.title`)
- Support RTL languages when applicable

**State Management:**

- Use Pinia for all shared state
- Organize stores by domain (e.g., `useUserStore`, `useAuthStore`)
- Always provide strong TypeScript types for state, getters, and actions

**Responsiveness:**

- Design mobile-first
- Test on common viewport sizes (320px, 768px, 1024px, 1440px)
- Use responsive Tailwind classes (`sm:`, `md:`, `lg:`, `xl:`)

**Rationale**: Consistent UX reduces user confusion, improves satisfaction, and reduces support costs. Accessibility is both a legal requirement and a moral imperative. A design system accelerates development and ensures brand coherence.

### VI. Performance & Scalability

**The system MUST be designed for production scale from day one:**

**Backend Performance:**

- Use reactive programming (Spring WebFlux + R2DBC) for non-blocking I/O
- NEVER call `.block()` in application code - embrace `Mono<T>` and `Flux<T>`
- Use database indexes on all foreign keys and frequently-queried columns
- Implement pagination for all collection endpoints (default page size: 20)
- Use caching for frequently-accessed, rarely-changing data
- Target: p95 response time < 200ms for API endpoints

**Frontend Performance:**

- Lazy-load routes and components (`defineAsyncComponent`)
- Use `v-memo` and `v-once` for static content
- Optimize images (use Astro's `<Image />` component for marketing site)
- Avoid shipping unnecessary JavaScript to the client
- Clean up side effects in `onUnmounted` (timers, listeners, subscriptions)
- Target: First Contentful Paint < 1.5s, Time to Interactive < 3.5s

**Database:**

- Use UUIDs (v4) for all primary keys
- Implement Row-Level Security (RLS) for multi-tenant isolation
- Create indexes on `tenant_id` and other filter columns
- Use Liquibase migrations for schema changes (never manual ALTER statements)
- Target: Query execution time < 50ms for simple queries, < 500ms for complex aggregations

**Infrastructure:**

- Design for horizontal scalability (stateless services)
- Use connection pooling for database access
- Implement rate limiting on public API endpoints

**Rationale**: Performance is a feature. Slow applications lose users. Reactive programming and proper indexing enable handling thousands of concurrent users on modest hardware. Lazy loading improves perceived performance significantly.

### VII. Observability & Monitoring

**The system MUST be observable in production:**

**Logging:**

- Use structured logging (JSON format in production)
- Log levels: DEBUG (dev only), INFO (important events), WARN (recoverable issues), ERROR (failures)
- ALWAYS log: authentication events, authorization failures, errors, and important business events
- NEVER log: passwords, tokens, PII (unless encrypted/masked)
- Include request correlation IDs in all log entries

**Metrics:**

- Expose Spring Boot Actuator endpoints (`/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`)
- Track: request count, response times, error rates, database connection pool usage
- Use Prometheus format for easy integration with monitoring tools

**Health Checks:**

- Implement liveness probe (is the service running?)
- Implement readiness probe (is the service ready to accept traffic?)
- Include dependency health (database, external APIs)

**Error Tracking:**

- Use global exception handlers (`@ControllerAdvice` for backend)
- Return consistent error format with `code`, `message`, and `errors` array
- Log full stack traces server-side, return sanitized errors to clients

**API Documentation:**

- Generate OpenAPI/Swagger docs from code (SpringDoc OpenAPI)
- Keep API docs in sync with implementation (generate from tests)

**Rationale**: Without observability, debugging production issues is impossible. Structured logging enables log aggregation and querying. Metrics enable proactive monitoring and alerting. Good error messages reduce time-to-resolution dramatically.

## Technology Standards

**This section defines the approved technology stack and MUST be updated when technologies change.**

**Backend:**

- Language: Kotlin 2.0.20+
- Framework: Spring Boot 3.3.4+ with Spring WebFlux (reactive)
- Database: PostgreSQL with Spring Data R2DBC
- Security: Spring Security + OAuth2 Resource Server + Keycloak 26.0.0+
- Migrations: Liquibase
- Testing: JUnit 5, Kotest, Testcontainers, MockK
- Build: Gradle 8.x with Kotlin DSL

**Frontend:**

- Languages: TypeScript 5.x
- Web App: Vue.js 3.5.17+ with Composition API
- Marketing Site: Astro 5.11.1+
- Styling: Tailwind CSS 4.1.11+
- UI Components: Shadcn-Vue (Reka UI), Lucide icons
- State: Pinia 3.0.3+
- Forms: Vee-Validate with Zod schemas
- Testing: Vitest, @testing-library/vue, Playwright (E2E)
- Build: Vite 7.0.4+
- Package Manager: pnpm 10.13.1+ (workspaces)

**Infrastructure:**

- Containers: Docker + Docker Compose (local dev)
- Database: PostgreSQL (official Docker image)
- Auth: Keycloak (Docker)
- Email: GreenMail / MailDev (local testing)
- CI/CD: GitHub Actions

**Code Quality:**

- Kotlin: Detekt
- JavaScript/TypeScript: Biome
- Security: OWASP Dependency Check
- Git Hooks: Lefthook

**Versioning Policy:**

- Follow Semantic Versioning (MAJOR.MINOR.PATCH)
- Breaking changes require MAJOR bump and migration guide

## Development Workflow

**All development MUST follow this workflow to ensure quality and consistency:**

### Branch Strategy

**Branch Naming:**

- Features: `feature/<description>` (e.g., `feature/user-authentication`)
- Fixes: `fix/<description>` (e.g., `fix/login-form-validation`)
- Docs: `docs/<description>` (e.g., `docs/update-readme`)
- Chores: `chore/<description>` (e.g., `chore/upgrade-gradle-wrapper`)
- Refactoring: `refactor/<description>` (e.g., `refactor/extract-user-service`)

**Main Branch Protection:**

- All changes MUST go through Pull Requests
- At least ONE approval required before merge
- ALL CI checks MUST pass (tests, linting, builds)

### Commit Messages

Follow Conventional Commits specification:

```conventionalcommit
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`

**Examples:**

- `feat(auth): ✨ add password reset functionality`
- `fix(api): 🐛 correct pagination query parameter`
- `docs(readme): 📝 update quickstart instructions`

**Commit Emojis** (optional but encouraged):

- ✨ (feat), 🐛 (fix), 📝 (docs), 🎨 (style), ♻️ (refactor), 🚀 (perf), 🧪 (test), 📦 (build), ⚙️ (ci), 🔧 (chore)

### Pre-Commit Checks (Lefthook)

**Runs automatically via git hooks:**

- Biome formatting check
- Changed files summary
- Lightweight linting

### Pre-Push Checks (Lefthook)

**Runs automatically before pushing:**

- Link checking
- Detekt static analysis
- Unit and integration tests
- Build verification

### Pull Request Process

**For the Author:**

1. Self-review the PR before requesting review
2. Write clear PR description: WHAT, WHY, HOW
3. Keep PRs small and focused (< 400 lines changed)
4. Ensure CI is green before requesting review
5. Link to related issue/spec document

**For the Reviewer:**

1. Understand the context (read spec, related issues)
2. Provide constructive, specific feedback
3. Review for: correctness, readability, security, performance, tests
4. Approve or request changes with clear guidance

**Merge Requirements:**

- At least ONE approval
- ALL CI checks passing
- ALL conversations resolved
- No merge conflicts

### Code Review Guidelines

**The Golden Rule**: Treat every review as a learning opportunity. Provide constructive, respectful, and clear feedback.

**Focus Areas:**

- Does the code follow Hexagonal Architecture?
- Are tests comprehensive and meaningful?
- Are security best practices followed?
- Is the code readable and maintainable?
- Are edge cases handled?
- Is performance acceptable?

## Quality Gates

**The following gates MUST pass before merging to main:**

### Build Gate

- `./gradlew build` succeeds without errors
- `pnpm run build` succeeds without errors
- No compiler warnings in production builds

### Test Gate

- ALL unit tests pass (`./gradlew test`, `pnpm test`)
- ALL integration tests pass (`./gradlew integrationTest`)
- Code coverage meets minimum thresholds (80% backend, 75% frontend)
- E2E tests pass for critical flows (on staging environment)

### Quality Gate

- Zero Detekt violations (`./gradlew detektAll`)
- Zero Biome violations (`pnpm check`)
- No `TODO` or `FIXME` comments without linked issues
- All public APIs have documentation comments

### Security Gate

- `pnpm audit` shows no high or critical vulnerabilities
- OWASP Dependency Check shows no critical vulnerabilities
- No hardcoded secrets detected (`scripts/check-secrets.sh`)
- All inputs validated and sanitized

### Architecture Gate

- Domain layer has zero framework dependencies
- All HTTP endpoints are in infrastructure layer
- All database access goes through repository interfaces
- No business logic in controllers or repositories

### Documentation Gate

- Public APIs documented with examples
- README updated if environment changes
- CHANGELOG updated with user-facing changes
- Migration guides provided for breaking changes

## Governance

**This Constitution is the highest authority for development practices.**

### Amendment Process

1. **Proposal**: Submit proposal as PR to `.specify/memory/constitution.md`
2. **Discussion**: Discuss in PR comments with team members
3. **Approval**: Requires consensus from at least 2 core team members
4. **Migration Plan**: If amendment affects existing code, include migration strategy
5. **Version Bump**: Follow semantic versioning for constitution itself
   - MAJOR: Backward incompatible changes (removing/redefining principles)
   - MINOR: New principles or material expansions
   - PATCH: Clarifications, typo fixes, non-semantic refinements
6. **Sync Templates**: Update all affected templates in `.specify/templates/`

### Compliance

- ALL Pull Requests MUST be verified against this Constitution
- Violations MUST be flagged in code review
- Complexity MUST be justified in writing (document in plan.md)
- Security violations are blocking and MUST be fixed before merge

### Relation to Other Documentation

- `.ruler/` directory contains detailed conventions referenced by this Constitution
- `docs/` Starlight site contains user-facing documentation and tutorials
- `.specify/templates/` contains templates that implement Constitution principles
- In case of conflict, this Constitution takes precedence

### Living Document

This Constitution is a living document. As the project evolves:

- Principles may be refined based on lessons learned
- Technology standards MUST be updated when stack changes
- New principles may be added as patterns emerge
- Outdated practices MUST be deprecated explicitly

**Version**: 1.0.0 | **Ratified**: 2025-10-20 | **Last Amended**: 2025-10-20
