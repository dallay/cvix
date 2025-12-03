<!--
================================================================================
SYNC IMPACT REPORT
================================================================================
Version change: 0.0.0 → 1.0.0 (MAJOR - Initial ratification)

Modified principles: N/A (initial creation)

Added sections:
  - Core Principles (4 principles)
  - Development Standards
  - Quality Gates
  - Governance

Removed sections: N/A

Templates requiring updates:
  ✅ plan-template.md - Constitution Check section aligns with principles
  ✅ spec-template.md - User scenarios and requirements align with UX/testing principles
  ✅ tasks-template.md - Phase structure supports testing-first and quality gates

Follow-up TODOs: None
================================================================================
-->

# CVIX SaaS Template Constitution

## Core Principles

### I. Code Quality First

All code MUST adhere to established conventions and maintainability standards.

**Non-negotiable rules:**
- Follow language-specific conventions: Kotlin Coding Conventions for backend, TypeScript strict mode for frontend
- Use static analysis tools: Detekt for Kotlin, Biome for TypeScript/JavaScript
- No code merges with unresolved linter errors or warnings (unless explicitly justified)
- Functions MUST be small, focused, and follow single responsibility principle
- Prefer immutability: use `val` over `var` (Kotlin), `const` over `let` (TypeScript)
- No `any` type in TypeScript; no `!!` operator in Kotlin
- All public APIs MUST have JSDoc/KDoc documentation

**Rationale:** Consistent, readable code reduces cognitive load, accelerates onboarding, and prevents technical debt accumulation.

### II. Testing Standards (NON-NEGOTIABLE)

Every feature MUST have appropriate test coverage following the testing pyramid.

**Non-negotiable rules:**
- Unit tests MUST cover all business logic and utility functions
- Integration tests MUST verify component interactions (services ↔ database, API ↔ services)
- E2E tests MUST cover critical user flows (authentication, core features)
- Test names MUST follow `should do something when condition` pattern
- Backend: Use JUnit 5, Kotest assertions, MockK for mocking, Testcontainers for integration
- Frontend: Use Vitest, Testing Library for component tests, Playwright for E2E
- No PRs merged without passing CI tests
- Coverage thresholds: Aim for meaningful coverage, not arbitrary percentages

**Rationale:** Tests serve as living documentation, prevent regressions, and enable confident refactoring.

### III. User Experience Consistency

All user-facing interfaces MUST follow the design system and provide consistent, accessible experiences.

**Non-negotiable rules:**
- Use semantic design tokens from the design system (no hardcoded colors like `bg-gray-900`)
- All interactive elements MUST be keyboard-accessible
- Forms MUST use manual validation on blur (not aggressive real-time validation)
- Error messages MUST be clear, actionable, and user-friendly
- Loading states MUST be indicated for async operations
- Support both light and dark themes via CSS custom properties
- Internationalization: All user-facing text MUST use i18n functions (`$t()`)
- Mobile-responsive by default; test on multiple viewport sizes

**Rationale:** Consistent UX builds user trust, reduces support burden, and ensures accessibility compliance.

### IV. Performance Requirements

All features MUST meet performance thresholds appropriate to their context.

**Non-negotiable rules:**
- API response times: p95 < 200ms for read operations, p95 < 500ms for write operations
- Frontend bundle size: Monitor and justify increases > 10KB gzipped
- No blocking operations in reactive pipelines (WebFlux): Never use `.block()` in application code
- Database queries MUST use indexes; no N+1 query patterns
- Images MUST be optimized (use Astro's `<Image />` component, appropriate formats)
- Lighthouse scores: Aim for 90+ on Performance, Accessibility, Best Practices
- Cache appropriately: memoize expensive computations, use HTTP caching headers

**Rationale:** Performance directly impacts user satisfaction, SEO rankings, and infrastructure costs.

## Development Standards

### Code Review Requirements

- All PRs MUST be reviewed by at least one team member
- Reviews MUST verify compliance with all four core principles
- Reviewers MUST check for security implications (OWASP Top 10 awareness)
- Authors MUST self-review before requesting reviews

### Branch & Commit Standards

- Branch naming: `feature/<description>`, `fix/<description>`, `chore/<description>`
- Commits MUST follow Conventional Commits format: `type(scope): description`
- Commits SHOULD include appropriate emoji (✨ feat, 🐛 fix, 📝 docs, ♻️ refactor)

### Security Baseline

- No hardcoded secrets; use environment variables or secret management
- Parameterized queries only; no string concatenation for SQL
- Input validation on all external data
- Output encoding to prevent XSS
- HTTPS only; secure cookie attributes (`HttpOnly`, `Secure`, `SameSite`)

## Quality Gates

### Pre-Commit (Automated via Lefthook)

- [ ] Biome lint/format passes
- [ ] No secrets detected in staged files

### Pre-Push (Automated via Lefthook)

- [ ] All unit tests pass
- [ ] Detekt analysis passes (Kotlin)
- [ ] TypeScript strict compilation passes

### PR Merge Requirements

- [ ] CI pipeline passes (tests, lint, build)
- [ ] At least one approval from reviewer
- [ ] No unresolved review comments
- [ ] Branch is up-to-date with target branch

### Release Requirements

- [ ] All quality gates passed
- [ ] E2E tests pass against staging environment
- [ ] Performance benchmarks within thresholds
- [ ] Security scan shows no critical/high vulnerabilities

## Governance

### Amendment Process

1. Propose changes via PR to this constitution file
2. Document rationale and impact assessment
3. Require approval from at least two maintainers
4. Update version according to semantic versioning:
   - MAJOR: Principle removal or backward-incompatible changes
   - MINOR: New principle or section added
   - PATCH: Clarifications, wording improvements

### Compliance

- This constitution supersedes conflicting practices in other documentation
- All PRs MUST verify compliance; violations require explicit justification
- Quarterly review of principles for relevance and effectiveness

### Runtime Guidance

For detailed implementation guidance, refer to:
- `.ruler/` directory for language-specific conventions
- `docs/` for architecture and API documentation
- `CONTRIBUTING.md` for contribution workflow

**Version**: 1.0.0 | **Ratified**: 2025-12-02 | **Last Amended**: 2025-12-02
