# Implementation Plan: Secure Authentication System

**Branch**: `001-user-auth-system` | **Date**: October 20, 2025 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-user-auth-system/spec.md`

**Note**: This implementation plan follows the Hexagonal Architecture pattern and constitution principles.

## Summary

Build a production-ready authentication system enabling users to register, login, manage sessions, and authenticate via email/password or federated identity providers (Google, Microsoft, GitHub).

The system integrates with Keycloak for OIDC/OAuth2 flows, implements automatic token refresh with transparent UX, supports multi-device session management with granular control, and maintains real-time user context across the application.

The frontend uses Vue 3 with feature-driven screaming architecture, Pinia stores for state, and shadcn-vue components. The backend uses Spring Boot with WebFlux for reactive processing, R2DBC for non-blocking database access, and Spring Security with OAuth2 Resource Server integration.

All authentication operations delegate to Keycloak; the application orchestrates UX flows, validation, error handling, and session semantics.

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
- **Framework**: Vue 3 Composition API with `<script setup lang="ts"> </script>`
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

```text
client/apps/webapp/src/authentication/
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

```text
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

## Integration Points

- Keycloak OIDC endpoints (authorization code flow with PKCE)
- PostgreSQL database (user profiles, sessions, audit events)
- IP geolocation service (for session location metadata)

**Updated Session Management Requirement**:

- Consolidated session recovery and persistence requirements to ensure clarity and avoid duplication. The system must:

  - Automatically restore sessions within the validity period.
  - Prompt users with clear messages when sessions expire.
  - Support "Remember Me" functionality for extended session persistence.

- Support 10,000+ concurrent users

- **domain/**: Modelos, constantes y excepciones de autenticación.

1. Load the specification, data model, and API contracts
