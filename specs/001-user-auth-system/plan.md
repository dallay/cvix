# Implementation Plan: Secure Authentication System

**Branch**: `001-user-auth-system` | **Date**: October 20, 2025 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-user-auth-system/spec.md`

**Note**: This implementation plan follows the Hexagonal Architecture pattern and constitution principles.

## Summary

Build a production-ready authentication system enabling users to register, login, manage sessions, and authenticate via email/password or federated identity providers (Google, Microsoft, GitHub). The system integrates with Keycloak for OIDC/OAuth2 flows, implements automatic token refresh with transparent UX, supports multi-device session management with granular control, and maintains real-time user context across the application. The frontend uses Vue 3 with feature-driven screaming architecture, Pinia stores for state, and shadcn-vue components. The backend uses Spring Boot with WebFlux for reactive processing, R2DBC for non-blocking database access, and Spring Security with OAuth2 Resource Server integration. All authentication operations delegate to Keycloak; the application orchestrates UX flows, validation, error handling, and session semantics.

## Technical Context

**Backend:**

- **Language/Version**: Kotlin 2.0.20+ with Spring Boot 3.3.4+
- **Framework**: Spring Boot WebFlux (reactive), Spring Security with OAuth2 Resource Server
- **Primary Dependencies**:
  - Spring Security OAuth2 Resource Server
  - Spring Data R2DBC (PostgreSQL)
  - Keycloak Admin Client (for user management integration)
  - Spring Boot Actuator (observability)
  - Liquibase (database migrations)
- **Storage**: PostgreSQL with R2DBC (reactive, non-blocking)
- **Testing**: JUnit 5, Kotest, Testcontainers (PostgreSQL, Keycloak), MockK, WebTestClient
- **Authentication Provider**: Keycloak 26.0.0+ (Docker container for local dev)
- **Architecture Pattern**: Hexagonal Architecture (Ports & Adapters) with CQRS
- **Security**: JWT tokens (RS256), HTTP-only secure cookies, CSRF protection, rate limiting
- **Performance Goals**: <200ms p95 response time for auth endpoints, handle 1000 concurrent auth requests
- **Constraints**:
  - All token operations must be non-blocking (reactive)
  - Session operations must complete within 2 seconds
  - Token refresh must be transparent (99% success rate)

**Frontend:**

- **Language/Version**: TypeScript 5.x with Vue 3.5.17+
- **Framework**: Vue 3 Composition API with `<script setup lang="ts">`
- **Architecture Pattern**: Feature-driven screaming architecture (bounded contexts)
- **Primary Dependencies**:
  - Pinia 3.0.3+ (state management)
  - Axios (HTTP client with interceptors)
  - VeeValidate with Zod schemas (form validation)
  - shadcn-vue (UI components based on Reka UI)
  - vue-i18n (internationalization)
  - Lucide icons
- **Styling**: Tailwind CSS 4.1.11+ (utility-first)
- **Testing**: Vitest, @testing-library/vue, Playwright (E2E)
- **Build Tool**: Vite 7.0.4+
- **Target Platform**: Modern evergreen browsers (Chrome, Firefox, Safari, Edge)
- **Performance Goals**: First Contentful Paint <1.5s, Time to Interactive <3.5s
- **Constraints**:
  - Zero token exposure in localStorage or browser history
  - Session state changes propagate within 500ms
  - All components must be keyboard accessible (a11y)
  - Form validation must be real-time with clear feedback

**Feature Structure (Frontend):**

```
client/apps/web/src/features/authentication/
├── domain/              # Pure TypeScript domain models, zero framework deps
│   ├── models/          # User, Session, AuthEvent entities
│   ├── errors/          # Domain-specific error types
│   └── validators/      # Pure validation logic (Zod schemas)
├── application/         # Use cases, framework-agnostic
│   ├── commands/        # RegisterUser, LoginUser, LogoutUser, RefreshToken
│   ├── queries/         # GetCurrentUser, GetActiveSessions
│   └── services/        # AuthService interface (port)
├── infrastructure/      # Adapters - framework integration
│   ├── http/            # Axios HTTP client, interceptors
│   ├── storage/         # Cookie/session storage adapters
│   └── keycloak/        # Keycloak OIDC integration
└── presentation/        # Vue components and composables
    ├── components/      # LoginForm, RegisterForm, SessionList, etc.
    ├── composables/     # useAuth, useSession, useTokenRefresh
    ├── pages/           # LoginPage, RegisterPage, SessionManagementPage
    └── stores/          # authStore (Pinia)
```

**Backend Structure (implementada):**

```
server/engine/src/main/kotlin/com/loomify/engine/authentication/
├── domain/
│   ├── AccessToken.kt
│   ├── AuthoritiesConstants.kt
│   ├── RefreshToken.kt
│   ├── RefreshTokenManager.kt
│   ├── Role.kt
│   ├── Roles.kt
│   ├── UserAuthenticationException.kt
│   ├── UserAuthenticator.kt
│   ├── UserAuthenticatorLogout.kt
│   ├── UserSession.kt
│   ├── Username.kt
│   └── error/                  # Excepciones de autenticación
├── application/
│   ├── AuthenticateUserQueryHandler.kt
│   ├── AuthenticatedUser.kt
│   ├── RefreshTokenQueryHandler.kt
│   ├── UserAuthenticatorService.kt
│   └── logout/
│       ├── UserLogoutCommand.kt
│       ├── UserLogoutCommandHandler.kt
│       └── UserLogoutService.kt
│   └── query/
│       ├── AuthenticateUserQuery.kt
│       ├── GetUserSessionQuery.kt
│       ├── GetUserSessionQueryHandler.kt
│       └── RefreshTokenQuery.kt
├── infrastructure/
│   ├── ApplicationSecurityProperties.kt
│   ├── AudienceValidator.kt
│   ├── AuthenticationExceptionAdvice.kt
│   ├── ClaimExtractor.kt
│   ├── Claims.kt
│   ├── CustomClaimConverter.kt
│   ├── JwtGrantedAuthorityConverter.kt
│   ├── OAuth2Configuration.kt
│   ├── SecurityConfiguration.kt
│   └── cookie/                 # Utilidades para cookies de autenticación
│   └── csrf/                   # CSRF token handler
│   └── filter/                 # Filtros WebFlux y JWT
│   └── http/                   # Controladores REST y modelos de request
│   └── mapper/                 # Mapeo de respuestas
│   └── persistence/            # Repositorios R2DBC y Keycloak
```

Cada capa sigue la arquitectura hexagonal:
- **domain/**: Modelos, constantes y excepciones de autenticación.
- **application/**: Handlers, servicios y comandos/queries CQRS.
- **infrastructure/**: Adaptadores y configuración Spring Boot, utilidades, controladores y persistencia.

Esta estructura documenta fielmente lo que está implementado hasta ahora y sirve como referencia para el equipo.

**Integration Points**:
- Keycloak OIDC endpoints (authorization code flow with PKCE)
- PostgreSQL database (user profiles, sessions, audit events)
- IP geolocation service (for session location metadata)

**Project Type**: Full-stack web application (monorepo with separate frontend/backend)
**Scale/Scope**:
- Support 10,000+ concurrent users
- 8 user stories (3 P1, 4 P2, 1 P3)
- 38 functional requirements
- 4 key entities (User, Session, AuthEvent, FederatedIdentityLink)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Hexagonal Architecture (Ports & Adapters)

- ✅ **PASS**: Feature follows clean architecture layering
  - Domain layer: Pure Kotlin/TypeScript entities with zero framework dependencies
  - Application layer: CQRS commands/queries, framework-agnostic use cases
  - Infrastructure layer: Spring Boot controllers, R2DBC repositories, Keycloak adapters
  - Dependencies point inward: domain ← application ← infrastructure

### II. Test-Driven Development

- ✅ **PASS**: TDD approach required for all components
  - Unit tests: Domain models, validators, command/query handlers
  - Integration tests: Repository implementations, HTTP controllers, Keycloak integration
  - E2E tests: Complete auth flows (register, login, logout, session management)
  - Coverage targets: 80% backend, 75% frontend
  - All business logic will be tested before implementation

### III. Code Quality & Static Analysis

- ✅ **PASS**: Quality tools configured
  - Backend: Detekt with project-specific rules, zero violations required
  - Frontend: Biome for linting/formatting, TypeScript strict mode
  - Pre-commit hooks via Lefthook
  - All code must pass `./gradlew detektAll` and `pnpm check`

### IV. Security-First Development

- ✅ **PASS**: Security best practices enforced
  - Access control: All authorization checks on backend (Spring Security)
  - Cryptography: Keycloak handles password hashing (Argon2), JWT tokens with RS256
  - Injection prevention: R2DBC parameterized queries, Vue template escaping
  - Security headers: CSP, HSTS, X-Content-Type-Options, X-Frame-Options
  - Cookies: HTTP-only, Secure, SameSite=Strict
  - Input validation: All user input validated on client and server
  - Rate limiting: 5 failed attempts per email within 15-minute window
  - CSRF protection: Keycloak CSRF tokens
  - Dependencies: pnpm audit and OWASP Dependency Check in CI

### V. User Experience Consistency

- ✅ **PASS**: UX standards met
  - Design system: shadcn-vue components with Tailwind CSS
  - Accessibility: Semantic HTML, keyboard navigation, ARIA attributes, screen reader tested
  - i18n: vue-i18n for all user-facing text
  - State management: Pinia stores organized by domain
  - Responsive: Mobile-first design, tested at 320px, 768px, 1024px, 1440px viewports

### VI. Performance & Scalability

- ✅ **PASS**: Performance requirements defined
  - Backend: Reactive (WebFlux + R2DBC), no `.block()` calls, pagination on collections
  - Frontend: Lazy loading, code splitting, v-memo for static content
  - Database: UUID primary keys, indexes on foreign keys and query columns, Row-Level Security for multi-tenancy
  - Targets: <200ms p95 API response, <1.5s FCP, <3.5s TTI, 1000 concurrent users

### VII. Observability & Monitoring

- ✅ **PASS**: Observability built-in
  - Logging: Structured JSON logs, correlation IDs, auth events logged (no PII)
  - Metrics: Spring Boot Actuator endpoints, Prometheus format
  - Health checks: Liveness and readiness probes with dependency health
  - Error tracking: Global exception handlers, consistent error format
  - API docs: SpringDoc OpenAPI generated from code

### Technology Standards

- ✅ **PASS**: All technologies match approved stack
  - Backend: Kotlin 2.0.20+, Spring Boot 3.3.4+, WebFlux, R2DBC, Keycloak 26.0.0+
  - Frontend: TypeScript 5.x, Vue 3.5.17+, Pinia 3.0.3+, shadcn-vue, Tailwind CSS 4.1.11+
  - Testing: JUnit 5, Kotest, Testcontainers, Vitest, Playwright
  - Build: Gradle 8.x with Kotlin DSL, Vite 7.0.4+, pnpm 10.13.1+

**Constitution Compliance**: ✅ ALL GATES PASSED - No violations or exceptions needed.

## Project Structure

### Documentation (this feature)

```text
specs/001-user-auth-system/
├── spec.md              # Feature specification (COMPLETE)
├── plan.md              # This file (implementation plan)
├── research.md          # Phase 0 output - technology decisions and patterns
├── data-model.md        # Phase 1 output - entity definitions and relationships
├── quickstart.md        # Phase 1 output - developer getting started guide
├── contracts/           # Phase 1 output - API contracts (OpenAPI specs)
│   ├── auth-api.yaml    # Authentication endpoints
│   ├── session-api.yaml # Session management endpoints
│   └── user-api.yaml    # User profile endpoints
├── checklists/          # Quality validation
│   └── requirements.md  # Specification quality checklist (COMPLETE)
└── tasks.md             # Phase 2 output - NOT created by /speckit.plan
```

### Source Code (repository root)

**Full-stack web application structure:**

```text
client/apps/web/src/features/authentication/
├── domain/
│   ├── models/
│   │   ├── User.ts              # User entity (email, name, status, roles)
│   │   ├── Session.ts           # Session entity (tokens, device, location)
│   │   ├── AuthEvent.ts         # Authentication event (type, outcome, timestamp)
│   │   └── FederatedIdentity.ts # Federated provider link
│   ├── errors/
│   │   ├── AuthenticationError.ts    # Base auth error
│   │   ├── InvalidCredentialsError.ts
│   │   ├── SessionExpiredError.ts
│   │   └── RateLimitError.ts
│   └── validators/
│       ├── registrationSchema.ts     # Zod schema for registration
│       ├── loginSchema.ts            # Zod schema for login
│       └── passwordValidator.ts      # OWASP password validation
├── application/
│   ├── commands/
│   │   ├── RegisterUserCommand.ts
│   │   ├── LoginUserCommand.ts
│   │   ├── LogoutUserCommand.ts
│   │   ├── RefreshTokenCommand.ts
│   │   └── TerminateSessionCommand.ts
│   ├── queries/
│   │   ├── GetCurrentUserQuery.ts
│   │   ├── GetActiveSessionsQuery.ts
│   │   └── GetAuthEventsQuery.ts
│   └── services/
│       ├── IAuthService.ts           # Port (interface)
│       ├── ISessionService.ts        # Port (interface)
│       └── ITokenService.ts          # Port (interface)
├── infrastructure/
│   ├── http/
│   │   ├── authHttpClient.ts         # Axios instance with interceptors
│   │   ├── tokenInterceptor.ts       # Auto-refresh on 401
│   │   └── errorInterceptor.ts       # Error normalization
│   ├── storage/
│   │   ├── cookieStorage.ts          # HTTP-only cookie adapter
│   │   └── memoryCache.ts            # Optional in-memory token cache
│   └── keycloak/
│       ├── keycloakAuthService.ts    # AuthService implementation
│       ├── keycloakSessionService.ts # SessionService implementation
│       └── oidcClient.ts             # OIDC flow handler (PKCE)
└── presentation/
    ├── components/
    │   ├── LoginForm.vue             # Email/password login form
    │   ├── RegisterForm.vue          # Registration form with validation
    │   ├── FederatedLoginButtons.vue # OAuth provider buttons
    │   ├── SessionList.vue           # Active sessions table
    │   ├── SessionItem.vue           # Single session display
    │   ├── PasswordStrengthMeter.vue # Real-time password feedback
    │   └── AuthGuard.vue             # Route protection component
    ├── composables/
    │   ├── useAuth.ts                # Main auth composable
    │   ├── useSession.ts             # Session management
    │   ├── useTokenRefresh.ts        # Automatic token refresh logic
    │   ├── useAuthRedirect.ts        # Deep linking after login
    │   └── useAuthValidation.ts      # Form validation helpers
    ├── pages/
    │   ├── LoginPage.vue             # /auth/login
    │   ├── RegisterPage.vue          # /auth/register
    │   ├── SessionManagementPage.vue # /auth/sessions
    │   ├── ForgotPasswordPage.vue    # /auth/forgot-password
    │   └── OAuthCallbackPage.vue     # /auth/callback
    └── stores/
        └── authStore.ts              # Pinia store (user context, auth state)

server/engine/src/main/kotlin/com/loomify/engine/authentication/
├── domain/
│   ├── AccessToken.kt
│   ├── AuthoritiesConstants.kt
│   ├── RefreshToken.kt
│   ├── RefreshTokenManager.kt
│   ├── Role.kt
│   ├── Roles.kt
│   ├── UserAuthenticationException.kt
│   ├── UserAuthenticator.kt
│   ├── UserAuthenticatorLogout.kt
│   ├── UserSession.kt
│   ├── Username.kt
│   └── error/                  # Excepciones de autenticación
├── application/
│   ├── AuthenticateUserQueryHandler.kt
│   ├── AuthenticatedUser.kt
│   ├── RefreshTokenQueryHandler.kt
│   ├── UserAuthenticatorService.kt
│   └── logout/
│       ├── UserLogoutCommand.kt
│       ├── UserLogoutCommandHandler.kt
│       └── UserLogoutService.kt
│   └── query/
│       ├── AuthenticateUserQuery.kt
│       ├── GetUserSessionQuery.kt
│       ├── GetUserSessionQueryHandler.kt
│       └── RefreshTokenQuery.kt
├── infrastructure/
│   ├── ApplicationSecurityProperties.kt
│   ├── AudienceValidator.kt
│   ├── AuthenticationExceptionAdvice.kt
│   ├── ClaimExtractor.kt
│   ├── Claims.kt
│   ├── CustomClaimConverter.kt
│   ├── JwtGrantedAuthorityConverter.kt
│   ├── OAuth2Configuration.kt
│   ├── SecurityConfiguration.kt
│   └── cookie/                 # Utilidades para cookies de autenticación
│   └── csrf/                   # CSRF token handler
│   └── filter/                 # Filtros WebFlux y JWT
│   └── http/                   # Controladores REST y modelos de request
│   └── mapper/                 # Mapeo de respuestas
│   └── persistence/            # Repositorios R2DBC y Keycloak
```

Cada capa sigue la arquitectura hexagonal:
- **domain/**: Modelos, constantes y excepciones de autenticación.
- **application/**: Handlers, servicios y comandos/queries CQRS.
- **infrastructure/**: Adaptadores y configuración Spring Boot, utilidades, controladores y persistencia.

Esta estructura documenta fielmente lo que está implementado hasta ahora y sirve como referencia para el equipo.

## Complexity Tracking

**No violations detected** - All constitution gates passed. No complexity justification required.

---

## Phase 1: Design & Contracts ✅ COMPLETE

All Phase 1 artifacts have been generated successfully:

### Generated Artifacts

1. **Data Model** (`data-model.md`) ✅
   - 4 core entities defined: User, Session, AuthEvent, FederatedIdentity
   - Complete properties, validation rules, enumerations, and state transitions
   - Entity Relationship Diagram (Mermaid)
   - Database indexes and Row-Level Security (RLS) policies
   - JSON examples for each entity
   - Validation against functional requirements and success criteria

2. **API Contracts** (`contracts/`) ✅
   - **`auth-api.yaml`**: Authentication and registration endpoints (OpenAPI 3.0.3)
     - `/auth/register` - User registration
     - `/auth/login` - Email/password login
     - `/auth/federated/initiate` - Start OAuth flow
     - `/auth/federated/callback` - Handle OAuth callback
     - `/auth/logout` - Session termination
     - `/auth/token/refresh` - Token refresh
     - `/auth/token/validate` - Token validation
   - **`session-api.yaml`**: Session management endpoints
     - `GET /sessions` - List all user sessions
     - `GET /sessions/{sessionId}` - Get session details
     - `DELETE /sessions/{sessionId}` - Terminate specific session
     - `POST /sessions/terminate-all` - Terminate all other sessions
   - **`user-api.yaml`**: User profile endpoints
     - `GET /user/profile` - Get user profile
     - `PATCH /user/profile` - Update user profile
     - `GET /user/profile/linked-providers` - List linked OAuth providers
     - `DELETE /user/profile/linked-providers` - Unlink OAuth provider

   All contracts include:
   - Complete request/response schemas
   - Multiple realistic examples
   - Error responses with machine-readable codes
   - Security schemes (Bearer token, HTTP-only cookies)
   - Rate limiting documentation
   - Performance targets and requirements mapping

3. **Developer Quickstart** (`quickstart.md`) ✅
   - Complete local development environment setup
   - Step-by-step instructions for Keycloak, PostgreSQL, backend, frontend
   - API testing examples (curl commands)
   - Multi-device session testing scenarios
   - Troubleshooting guide
   - Useful commands reference

### Phase 1 Verification

- ✅ Data model aligns with all 8 user stories
- ✅ API contracts satisfy all 38 functional requirements
- ✅ Entity relationships support multi-device session management
- ✅ Validation rules enforce security best practices
- ✅ State transitions documented for User and Session entities
- ✅ Database schema includes proper indexes and RLS policies
- ✅ OpenAPI contracts include security schemes and error handling
- ✅ Quickstart guide enables immediate developer onboarding

### Next Steps

**Phase 2 will be initiated via separate `/speckit.tasks` command**, which will:
1. Load the specification, data model, and API contracts
2. Generate a comprehensive task breakdown (`tasks.md`)
3. Organize tasks by priority (P1 → P2 → P3)
4. Map each task to test-first development workflow
5. Provide estimated effort and dependencies

**Do not proceed to implementation** until Phase 2 tasks are generated and reviewed.
