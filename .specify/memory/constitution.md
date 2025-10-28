# Loomify SaaS Template Constitution

<!--
Sync Impact Report - Version 1.1.0 (Principles Refinement)
===========================================================
Version Change: 1.0.0 → 1.1.0
Principles Modified:
  - II. Test-Driven Development (TDD) - Added specific coverage targets, test naming conventions, and tool specifications
  - III. Code Quality & Static Analysis - Enhanced with concrete rules, line limits, and documentation requirements
  - V. User Experience Consistency - Added viewport specifications, design token requirements, and RTL support
  - VI. Performance & Scalability - Added specific performance metrics and targets
Rationale: MINOR bump - material expansion of existing principles with actionable requirements
Added Sections: None (refinement only)
Removed Sections: None
Templates Status:
  ✅ plan-template.md - Already aligned with constitution principles (constitution check section present)
  ✅ spec-template.md - Already aligned (user story prioritization and acceptance criteria present)
  ✅ tasks-template.md - Already aligned (organized by user story with test-first approach)
Follow-up TODOs: None
Last Updated: 2025-10-28
-->

## Core Principles

### I. Hexagonal Architecture (Ports & Adapters)

**All backend features MUST follow Clean Architecture layering:**

- **Domain Layer**: Pure Kotlin business logic with zero framework dependencies. Contains entities, value objects, domain events, exceptions, and repository interfaces (ports).
- **Application Layer**: Framework-agnostic use cases implementing CQRS (commands/queries). Orchestrates domain logic without knowledge of infrastructure details.
- **Infrastructure Layer**: Adapters connecting to external systems (HTTP controllers, R2DBC repositories, external APIs). This is the ONLY layer permitted to use Spring Boot and framework-specific features.

**Dependency Rule**: Dependencies MUST point inward. Domain depends on nothing. Application depends on domain. Infrastructure depends on both but neither depends on infrastructure.

**Rationale**: This architecture ensures testability, maintainability, and the ability to swap implementations (e.g., database, web framework) without touching business logic. It enforces separation of concerns and makes the codebase resilient to framework changes.

### II. Test-Driven Development (TDD)

**TDD is mandatory for all new features. Tests MUST be written before implementation code.**

**TDD Workflow (NON-NEGOTIABLE):**

1. **Write tests first** → User approves acceptance criteria → Tests fail (red)
2. **Implement minimal code** to make tests pass (green)
3. **Refactor** while keeping tests green

**Test Pyramid MUST be maintained:**

- **Base (largest)**: Unit tests - fast (<100ms per test), isolated, testing individual functions/components
  - Mock ALL external dependencies using MockK (Kotlin) or Vitest mocks (TypeScript)
  - Each unit test MUST test ONE behavior
- **Middle**: Integration tests - verify component interactions (use Testcontainers for backend database tests)
  - Backend: `@DataR2dbcTest` for repository tests, `@WebFluxTest` for controller tests
  - Frontend: Test components with Pinia stores or composables making mock API calls
- **Top (smallest)**: E2E tests - critical user flows only using Playwright
  - E2E tests run against staging environment only
  - Use user-facing locators (`getByRole`, `getByLabel`, `getByText`)

**Coverage Requirements (NON-NEGOTIABLE):**

- Backend: Minimum 80% line coverage (Kover) - Domain layer MUST have 100% coverage
- Frontend: Minimum 75% line coverage (Vitest) for components, composables, and utilities
- ALL business logic (domain layer, service layer) MUST have unit tests
- ALL API contracts MUST have integration/contract tests
- ALL critical user flows (login, signup, core features) MUST have E2E tests

**Test Naming Conventions:**

- Kotlin: `` `should return user when user exists` `` (use backticks for readability)
- TypeScript: `"should render error message when validation fails"`
- Pattern: `should [expected behavior] when [condition]`
- Test files: `UserServiceTest.kt`, `UserProfileCard.test.ts`

**Test Organization:**

- Backend: Tests in `src/test/kotlin` mirroring `src/main/kotlin` structure
- Frontend: Tests co-located with components: `UserProfile.vue` + `UserProfile.test.ts`
- E2E tests: `client/e2e/` directory with `*.spec.ts` naming

**Test Annotations (Backend):**

- `@UnitTest` for isolated unit tests
- `@IntegrationTest` for integration tests
- Controller tests MUST extend `ControllerIntegrationTest` base class

**Rationale**: TDD ensures code is designed for testability from inception, reduces bugs discovered late, serves as living documentation, and enables confident refactoring. The test pyramid balances thoroughness with execution speed and maintenance cost. Specific coverage targets ensure business-critical code is verified.

### III. Code Quality & Static Analysis

**Code quality is enforced automatically and MUST pass before merge. Zero violations required.**

**Backend (Kotlin) - NON-NEGOTIABLE Rules:**

- Follow official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4 spaces for indentation (enforced by Detekt)
- Use `val` over `var` (immutability by default)
- STRICTLY AVOID the `!!` operator - null-safety is mandatory
  - Use `?.`, `?:`, `requireNotNull()`, or explicit null checks instead
- Prefer sealed classes/interfaces for restricted hierarchies (state, results, errors)
- Use `Result<T>` or sealed classes for error handling instead of throwing exceptions
- Functions with EXACTLY one statement (the return statement) MUST use expression body syntax
- Use top-level functions for pure, stateless utility operations
- Extension functions for enhancing existing classes
- Keep functions small (max 50 lines) and single-purpose
- Constants: `UPPER_SNAKE_CASE`
- Functions/Variables: `camelCase`
- Classes/Interfaces: `PascalCase`
- Run `./gradlew detektAll` - zero violations required
- Detekt configuration: `config/detekt.yml`

**Frontend (TypeScript/Vue) - NON-NEGOTIABLE Rules:**

- Use Biome for ALL linting and formatting (`pnpm check`)
- Use 2 spaces for indentation and always use semicolons
- TypeScript `strict` mode MUST be enabled in all `tsconfig.json` files
- STRICTLY AVOID `any` - use `unknown` with type guards or proper types instead
- Use `<script setup lang="ts">` for ALL Vue components
- Prefer `type` over `interface` for object shapes (unless declaration merging needed)
- Use absolute imports with `@/` alias (configured in `tsconfig.json`)
- Prefer named exports over default exports
- Use arrow functions by default: `const fn = () => {}`
- Keep functions small (max 50 lines) and single-purpose
- File naming: `kebab-case.ts` or `PascalCase.vue`
- Types/Interfaces: `PascalCase`
- Variables/Functions: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Use `readonly` for immutability enforcement
- Use utility types (`Partial`, `Pick`, `Omit`) and `as const` for literal types

**Documentation Requirements:**

- ALL public APIs MUST have JSDoc/KDoc comments with:
  - Description of purpose
  - `@param` documentation for each parameter
  - `@return` documentation for return value
  - `@throws` documentation for exceptions (if applicable)
- Comment WHY not WHAT (code should be self-documenting)
- NO `TODO` or `FIXME` comments without linked GitHub issues

**Code Review Checklist:**

- Functions serve single purpose (Single Responsibility Principle)
- No magic numbers (use named constants)
- No duplicated code (DRY principle)
- Error cases handled explicitly
- Edge cases considered and tested
- Performance implications considered

**Rationale**: Consistent code style reduces cognitive load, accelerates code reviews, prevents entire classes of bugs (null pointer, type errors), and ensures long-term maintainability as the team scales. Strict null-safety and type-safety eliminate runtime errors. Small, focused functions are easier to understand, test, and reuse. Documentation enables API discoverability and correct usage.

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

**Frontend MUST provide a cohesive, accessible, and predictable user experience across all applications.**

**Design System (MANDATORY):**

- Use **Shadcn-Vue** (built on Reka UI) as the PRIMARY UI component library
- Use **Tailwind CSS 4.x** utility classes for ALL styling
- Maintain consistent design tokens:
  - Spacing scale: 4px base unit (0.5, 1, 1.5, 2, 2.5, 3, 4, 6, 8, 10, 12, 16, 20, 24, 32, 40, 48, 64, 80, 96)
  - Typography scale: rem-based (text-xs through text-9xl)
  - Color palette: Defined in `tailwind.config.js` (primary, secondary, accent, neutral, semantic)
- Custom components ONLY when Shadcn-Vue cannot fulfill the requirement
- Document custom components in Storybook or project docs

**Accessibility (a11y) - NON-NEGOTIABLE:**

- ALL interactive elements MUST be keyboard accessible (Tab, Enter, Space, Escape, Arrow keys)
- Use semantic HTML ALWAYS:
  - Navigation: `<nav>`, `<header>`
  - Content structure: `<main>`, `<article>`, `<section>`, `<aside>`, `<footer>`
  - Forms: `<form>`, `<label>`, `<fieldset>`, `<legend>`
  - Buttons: `<button>` (NOT `<div>` or `<span>` with click handlers)
- Provide descriptive `alt` attributes for ALL images (empty `alt=""` for decorative images)
- Use `aria-*` attributes where semantic HTML insufficient:
  - `aria-label`, `aria-labelledby`, `aria-describedby`
  - `aria-expanded`, `aria-selected`, `aria-checked` for custom components
  - `role` attribute for custom interactive elements
- Color contrast ratios MUST meet WCAG AA standards (4.5:1 for normal text, 3:1 for large text)
- Test with screen readers (NVDA on Windows, VoiceOver on macOS/iOS)
- Test keyboard-only navigation (no mouse)
- Focus indicators MUST be visible (never `outline: none` without replacement)

**Internationalization (i18n) - MANDATORY:**

- ALL user-facing text MUST use `vue-i18n` with `$t()` function
- NO hardcoded strings in templates or components
- Organize translation keys by domain: `auth.login.title`, `profile.settings.email`
- Translation files: `client/apps/*/locales/{lang}.json`
- Support RTL (right-to-left) languages:
  - Use logical properties (`margin-inline-start` instead of `margin-left`)
  - Test with `dir="rtl"` attribute
- Date/time formatting: Use `Intl.DateTimeFormat` API
- Number formatting: Use `Intl.NumberFormat` API

**State Management (Pinia) - MANDATORY:**

- Use **Pinia 3.x** for ALL shared state across components
- Organize stores by domain: `useAuthStore`, `useUserStore`, `useProjectStore`
- Store file structure: `client/apps/*/stores/{domain}.ts`
- ALWAYS provide TypeScript types for:
  - State interface
  - Getters return types
  - Actions parameters and return types
- Use composition API style: `defineStore(() => { ... })`
- Keep stores focused (Single Responsibility Principle)

**Responsiveness - MANDATORY:**

- Design mobile-first (start with smallest viewport)
- Test on standard breakpoints:
  - Mobile: 320px, 375px, 414px
  - Tablet: 768px, 834px, 1024px
  - Desktop: 1280px, 1440px, 1920px
- Use responsive Tailwind classes:
  - Mobile (default): no prefix
  - Tablet: `md:` (768px+)
  - Desktop: `lg:` (1024px+), `xl:` (1280px+), `2xl:` (1536px+)
- Images MUST be responsive: use `srcset` and `sizes` attributes or Astro `<Image />` component
- Touch targets MUST be ≥44px × 44px on mobile (WCAG guideline)
- Avoid horizontal scrolling on any viewport size

**Component Best Practices:**

- Name components using `PascalCase` (e.g., `UserProfileCard.vue`)
- Use `<script setup lang="ts">` syntax for ALL components
- Define props with `defineProps<T>()` and provide defaults with `withDefaults()`
- Explicitly declare emitted events with `defineEmits<T>()`
- Co-locate styles in `<style scoped>` blocks
- Use slots for composition over complex prop configurations

**Performance:**

- Lazy-load routes: Use `defineAsyncComponent()` for Vue components
- Lazy-load images: Use `loading="lazy"` attribute
- Use `v-memo` for lists with static content
- Use `v-once` for content that never changes
- Clean up subscriptions/timers in `onUnmounted()` lifecycle hook

**Rationale**: Consistent UX reduces user confusion, increases user satisfaction, and decreases support costs. Accessibility is legally required (ADA, WCAG) and morally imperative—everyone deserves access. A unified design system accelerates development velocity by 3-5x, ensures brand coherence across applications, and prevents design drift. Internationalization expands market reach. State management prevents prop-drilling and makes state predictable. Responsiveness is mandatory in a mobile-first world.

### VI. Performance & Scalability

**The system MUST be designed for production scale from day one. Performance is a feature.**

**Backend Performance (Spring Boot + WebFlux) - MANDATORY:**

- Use reactive programming (Spring WebFlux + R2DBC) for non-blocking I/O on ALL endpoints
- NEVER call `.block()` in application code - embrace `Mono<T>` and `Flux<T>` throughout
  - Exception: Test code only
- Use backpressure strategies for streams to prevent memory overflow
- Implement pagination for ALL collection endpoints:
  - Default page size: 20 items
  - Maximum page size: 100 items
  - Use cursor-based pagination for large datasets (>10k records)
- Use caching strategically:
  - Cache frequently-accessed, rarely-changing data (configuration, lookup tables)
  - Use Spring Cache abstraction with Redis backend for production
  - Cache invalidation MUST be explicit and tested
- **Performance Targets (NON-NEGOTIABLE):**
  - p50 response time: <100ms for simple queries
  - p95 response time: <200ms for API endpoints
  - p99 response time: <500ms for complex operations
  - Database query execution: <50ms for indexed queries, <500ms for complex aggregations
  - Throughput: Minimum 1000 requests/second per instance (simple endpoints)

**Frontend Performance (Vue 3 + Vite) - MANDATORY:**

- Lazy-load routes using dynamic imports: `component: () => import('./views/Dashboard.vue')`
- Lazy-load components with `defineAsyncComponent()` for modals, tabs, heavy widgets
- Use `v-memo` directive for lists with static content to skip re-renders
- Use `v-once` directive for content that never changes
- Optimize images:
  - Use WebP format with JPEG/PNG fallback
  - Provide `srcset` with multiple resolutions
  - Use `loading="lazy"` attribute for below-fold images
  - Use Astro `<Image />` component for automatic optimization (marketing site)
- Minimize JavaScript bundle size:
  - Code-split by route (automatic with Vue Router + Vite)
  - Tree-shake unused dependencies
  - Avoid importing entire libraries (e.g., import specific lodash functions)
- Clean up side effects in `onUnmounted()`:
  - Clear timers (`setInterval`, `setTimeout`)
  - Remove event listeners
  - Cancel subscriptions (API calls, WebSockets)
- Use Web Workers for CPU-intensive tasks (large data transformations, parsing)
- **Performance Targets (NON-NEGOTIABLE):**
  - First Contentful Paint (FCP): <1.5 seconds
  - Largest Contentful Paint (LCP): <2.5 seconds
  - Time to Interactive (TTI): <3.5 seconds
  - Cumulative Layout Shift (CLS): <0.1
  - First Input Delay (FID): <100ms
  - Total Bundle Size: <250KB gzipped (main bundle)
  - Lighthouse Performance Score: ≥90

**Database Performance (PostgreSQL + R2DBC) - MANDATORY:**

- Use UUID v4 for ALL primary keys (generated in application layer)
- Create indexes on ALL foreign keys and frequently-queried columns:
  - `CREATE INDEX idx_table_tenant_id ON table (tenant_id);`
  - Composite indexes for common query patterns
- Implement Row-Level Security (RLS) for multi-tenant data isolation:
  - Enable RLS on ALL tenant-scoped tables
  - Set `app.current_tenant` session variable on connection
- Use Liquibase for ALL schema changes (never manual `ALTER` statements)
- Optimize queries:
  - Use `EXPLAIN ANALYZE` to identify slow queries
  - Avoid N+1 queries (use JOINs or batch fetching)
  - Use database-level aggregations instead of fetching all data
  - Limit result sets with WHERE clauses
- **Performance Targets:**
  - Query execution: <50ms for simple indexed queries
  - Complex aggregations: <500ms
  - Connection pool: Min 5, Max 20 connections per instance
  - Connection acquisition: <10ms

**Infrastructure Scalability - MANDATORY:**

- Design services to be STATELESS (horizontal scaling ready)
  - NO in-memory session storage (use Redis or database)
  - NO local file storage (use S3 or equivalent)
- Use connection pooling for database access (HikariCP for R2DBC)
- Implement rate limiting on ALL public API endpoints:
  - Anonymous users: 100 requests/hour
  - Authenticated users: 1000 requests/hour
  - Configurable per endpoint via `application.yml`
- Use async processing for long-running tasks:
  - Email sending
  - Report generation
  - Batch operations
- Implement circuit breakers for external service calls (Resilience4j)
- Use API gateway pattern for request routing and load balancing

**Monitoring & Optimization:**

- Profile application under load before production deployment (JMeter, Gatling)
- Monitor JVM metrics: heap usage, GC pauses, thread count
- Monitor database metrics: connection pool usage, query duration, cache hit rate
- Set up alerts for performance degradation:
  - p95 response time exceeds SLO by 50%
  - Error rate >1%
  - Database connection pool exhaustion

**Rationale**: Performance directly impacts user satisfaction, conversion rates, and operational costs. Slow applications lose users (53% abandon sites taking >3s to load). Reactive programming enables handling 10-100x more concurrent users with the same hardware. Proper indexing and caching prevent database bottlenecks. Lazy loading and code-splitting reduce initial load time significantly. Horizontal scalability enables handling traffic spikes without downtime. Performance targets are based on industry standards (Google Core Web Vitals, RAIL model) and real-world user expectations.

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

**Version**: 1.1.0 | **Ratified**: 2025-10-20 | **Last Amended**: 2025-10-28
