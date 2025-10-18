# Technology Stack & Build System

## Backend Stack

- **Language**: Kotlin 2.0.20 with coroutines and reactive programming
- **Framework**: Spring Boot 3.3.4 with WebFlux (reactive web stack)
- **Database**: PostgreSQL with R2DBC for reactive database access
- **Security**: Spring Security + OAuth2 Resource Server + Keycloak 26.0.0
- **API Documentation**: SpringDoc OpenAPI 2.6.0
- **Database Migrations**: Liquibase for schema versioning
- **Build Tool**: Gradle 8.x with Kotlin DSL and multi-project setup

## Frontend Stack

- **Web App**: Vue.js 3.5+ with Composition API and TypeScript
- **Marketing Site**: Astro 5.12+ with Vue components for interactivity
- **Styling**: TailwindCSS 4.1+ with utility-first approach
- **State Management**: Pinia 3.0+ for reactive state
- **Form Validation**: Vee-Validate + Zod for type-safe forms
- **UI Components**: Reka UI component library + Lucide icons
- **Build Tool**: Vite 7.0+ for fast development and builds
- **Package Manager**: pnpm 10.15+ with workspace support

## Development Tools

- **Code Quality**: Biome (JS/TS), Detekt (Kotlin), Ktlint
- **Testing**: Vitest (frontend), JUnit 5 + Kotest (backend), Playwright (E2E)
- **Coverage**: Kover (Kotlin), Vitest coverage (frontend)
- **Security**: OWASP Dependency Check
- **Git Hooks**: Lefthook for pre-commit validation

## Common Commands

### Backend Development

```bash
# Start backend server
./gradlew :server:engine:bootRun

# Run all backend tests
./gradlew test

# Build backend
./gradlew build

# Clean build artifacts
./gradlew clean
```

### Frontend Development

```bash
# Install dependencies
pnpm install

# Start web app dev server
pnpm --filter @loomify/webapp dev

# Start marketing site dev server
pnpm --filter @loomify/marketing dev

# Run all frontend tests
pnpm test

# Build all frontend apps
pnpm build

# Lint all frontend code
pnpm lint
```

### Infrastructure

```bash
# Start local development infrastructure
docker compose up -d postgresql keycloak greenmail

# Stop all services
docker compose down

# View logs
docker compose logs -f [service-name]
```

### Full Stack Development

```bash
# Start everything (backend + frontend)
pnpm start

# Run all tests (frontend + backend)
pnpm test:all
```

## Node.js Requirements

- **Node.js**: >= 22.0.0
- **pnpm**: >= 10.0.0
- Use `.nvmrc` for consistent Node version across team

## JVM Requirements

- **JDK**: 21+ (required for Spring Boot 3.3+)
- **Gradle**: Uses wrapper, no separate installation needed
