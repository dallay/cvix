# Project Structure & Organization

## Monorepo Layout

```text
├── client/                 # Frontend monorepo (pnpm workspace)
│   ├── apps/              # Frontend applications
│   │   ├── webapp/        # Main Vue.js SPA (@loomify/webapp)
│   │   └── marketing/     # Astro marketing site (@loomify/marketing)
│   ├── packages/          # Shared frontend libraries
│   │   ├── logger/        # Logging utilities
│   │   ├── utilities/     # Common utility functions
│   │   └── tsconfig/      # Shared TypeScript configurations
│   └── config/            # Shared build configurations
├── server/                # Backend services (Gradle multi-project)
│   └── engine/            # Main Spring Boot application
├── shared/                # Shared Kotlin libraries
│   ├── common/            # Common Kotlin utilities
│   └── spring-boot-common/ # Spring Boot shared components
├── infra/                 # Infrastructure & Docker configs
├── docs/                  # Documentation site (Astro)
├── config/                # Build tool configurations
└── gradle/                # Gradle wrapper and libs catalog
```

## Backend Organization (`server/engine/`)

- **Domain-Driven Design**: Organize by business domains, not technical layers
- **Package Structure**: `com.loomify.engine.[domain].[layer]`
- **Reactive Architecture**: Use WebFlux, R2DBC, and reactive streams
- **Database Migrations**: Located in `src/main/resources/db/changelog/`
- **Configuration**: Environment-specific configs in `application-{profile}.yml`

## Frontend Organization

### Web App (`client/apps/webapp/`)

- **Pages**: Route-based components in `src/pages/`
- **Components**: Reusable UI components in `src/components/`
- **Stores**: Pinia stores in `src/stores/`
- **Composables**: Vue composables in `src/composables/`
- **Types**: TypeScript definitions in `src/types/`

### Marketing Site (`client/apps/marketing/`)

- **Content**: MDX content in `src/content/`
- **Layouts**: Astro layouts in `src/layouts/`
- **Components**: Vue/Astro components in `src/components/`
- **Assets**: Static assets in `src/assets/`

## Shared Libraries

### Frontend Packages (`client/packages/`)

- **@loomify/utilities**: Common utility functions
- **@loomify/logger**: Centralized logging
- **@loomify/tsconfig**: Shared TypeScript configurations

### Backend Shared (`shared/`)

- **common**: Domain-agnostic Kotlin utilities
- **spring-boot-common**: Spring Boot specific shared code

## Configuration Files

### Root Level

- `package.json`: Workspace configuration and scripts
- `pnpm-workspace.yaml`: pnpm workspace definition
- `build.gradle.kts`: Root Gradle build configuration
- `settings.gradle.kts`: Gradle project structure
- `compose.yaml`: Docker Compose for local development

### Code Quality

- `biome.json`: JavaScript/TypeScript linting and formatting
- `config/detekt.yml`: Kotlin static analysis
- `lefthook.yml`: Git hooks configuration
- `.editorconfig`: Editor configuration

## Naming Conventions

### Projects & Packages

- Frontend apps: `@loomify/[app-name]` (e.g., `@loomify/webapp`)
- Backend modules: `server:[module-name]` (e.g., `server:engine`)
- Shared libraries: `shared:[lib-name]` (e.g., `shared:common`)

### Files & Directories

- **Kebab-case**: For file and directory names
- **PascalCase**: For component files (Vue/React)
- **camelCase**: For utility functions and variables
- **UPPER_SNAKE_CASE**: For constants and environment variables

## Development Workflow

1. **Feature Development**: Create feature branches from `main`
2. **Code Organization**: Follow domain-driven design principles
3. **Testing**: Write tests alongside implementation
4. **Documentation**: Update relevant docs in `docs/` or `.ruler/`
5. **Quality Gates**: All code must pass linting, tests, and security checks
