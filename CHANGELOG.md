# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Workspace Selection Feature** - Complete frontend implementation for workspace selection functionality
  - Automatic workspace loading on user login (loads last selected or default workspace)
  - Manual workspace selection through an enhanced dropdown selector component
  - Workspace state management using Pinia with local storage persistence
  - Loading and error states with visual feedback
  - Retry logic with exponential backoff for failed workspace loads (3 attempts: 1s, 2s, 4s delays)
  - 5-minute cache TTL for workspace data to optimize performance
  - Comprehensive test coverage (>75%) across domain, application, and presentation layers
  - ARIA labels and keyboard navigation support for accessibility
  - Integration with existing authentication system (Keycloak OAuth2/OIDC)

### Technical Details

- **Domain Layer**: Pure TypeScript business logic with value objects (WorkspaceId, WorkspaceName) and selection service
- **Application Layer**: Composables (`useWorkspaceSelection`, `useWorkspaceLoader`) for orchestrating workspace operations
- **Infrastructure Layer**: Pinia store for state management, HTTP client for API integration, local storage adapter for persistence
- **Presentation Layer**: Vue.js components with Shadcn-Vue UI components (WorkspaceSelector, WorkspaceSelectorItem)
- **Testing**: Unit tests (Vitest), integration tests, E2E tests (Playwright) with 94% domain coverage, 100% application coverage

### Changed

- Enhanced workspace selector component with improved UX and visual feedback
- Updated router with navigation guard for automatic workspace loading on login

### Fixed

- Resolved 13 Biome linting warnings (noExplicitAny, noUselessConstructor)

---

## [1.0.0] - 2025-10-30

### Initial Release

- Base SaaS template with Spring Boot + Kotlin backend
- Vue.js 3 frontend with TypeScript
- Authentication system with Keycloak
- PostgreSQL database with Liquibase migrations
- Docker Compose infrastructure setup
- CI/CD with GitHub Actions
- Comprehensive documentation and development tools
