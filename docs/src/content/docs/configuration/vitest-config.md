---
title: "Vitest Configuration Guide"
description: "Guide to the Vitest configuration setup in this monorepo."
---

This monorepo uses Vitest's **projects feature** to manage tests across multiple packages and applications with a centralized configuration.

## Architecture

### Root Configuration (`/vitest.config.ts`)

The root configuration defines:

- **Global options**: `reporters`, `coverage`, and `globals`
- **Project references**: Points to all testable projects in the monorepo

```typescript
export default defineConfig({
  test: {
    globals: true,
    reporters: [...],      // Only at root level
    coverage: {...},       // Only at root level
    projects: [
      './client/apps/webapp',
      './client/apps/marketing',
      './client/packages/utilities',
    ]
  }
})
```

### Project Configurations

Each project uses `defineProject()` and automatically inherits global settings while providing project-specific options:

#### Webapp (`/client/apps/webapp/vitest.config.ts`)

- **Environment**: `jsdom` (for Vue component testing)
- **Name**: `webapp`
- Automatically inherits global reporters and coverage settings

#### Marketing (`/client/apps/marketing/vitest.config.ts`)

- **Environment**: `node` (for Astro SSR testing)
- **Name**: `marketing`
- Automatically inherits global reporters and coverage settings
- Project-specific coverage excludes are defined in root config

#### Utilities (`/client/packages/utilities/vitest.config.ts`)

- **Environment**: `node` (for library testing)
- **Name**: `utilities`
- Automatically inherits global reporters and coverage settings

## Usage

### Run All Tests

```bash
# From root - runs tests recursively in all packages
pnpm test

# Alternative - runs tests via root vitest config
vitest
```

### Run Tests for Specific Project

```bash
# Using the project flag
pnpm test:webapp      # or vitest --project=webapp
pnpm test:marketing   # or vitest --project=marketing
pnpm test:utilities   # or vitest --project=utilities
```

### Run Tests with UI

```bash
# All projects
pnpm test:ui

# Specific project
vitest --ui --project=webapp
```

### Run Tests with Coverage

```bash
# All projects - coverage collected globally
pnpm test:coverage

# Specific project
vitest run --coverage --project=utilities
```

## Benefits of Centralized Configuration

1. **Single Source of Truth**: Global options (reporters, coverage) defined once in root config
2. **No Duplication**: Projects inherit common settings via `extends: true`
3. **Selective Testing**: Run tests for specific projects without running all tests
4. **Parallel Execution**: Vitest can run project tests in parallel for better CI performance
5. **Unified Tooling**: Single Vitest instance manages all test suites
6. **Type Safety**: Better type inference across projects

## Configuration Hierarchy

```text
Root vitest.config.ts
├── Global options (reporters, coverage, globals)
├── Project: webapp
│   ├── Auto-inherits: globals, reporters, coverage
│   └── Overrides: environment=jsdom, name, setupFiles
├── Project: marketing
│   ├── Auto-inherits: globals, reporters, coverage
│   └── Overrides: environment=node, name, setupFiles
└── Project: utilities
  ├── Auto-inherits: globals, reporters, coverage
  └── Overrides: environment=node, name
```

**Important**: Coverage configuration is **only supported at the root level**. Project-specific coverage excludes must be added to the root `coverage.exclude` array with appropriate path prefixes.

## Vitest Documentation References

- [Projects Feature](https://vitest.dev/guide/projects.html)
- [Configuration Options](https://vitest.dev/config/)
- [Coverage Configuration](https://vitest.dev/config/#coverage)

## Migration Notes

This setup replaces the previous approach where each app had standalone configurations with duplicated settings. The shared configuration in `/client/config/vitest.config.shared.mjs` is now deprecated in favor of the projects feature.

### Key Changes

- ✅ Global reporters and coverage settings moved to root config
- ✅ Each project uses `defineProject()` instead of `defineConfig()`
- ✅ Projects automatically inherit root settings when referenced in `projects` array
- ✅ Obsolete root `vitest.setup.ts` removed
- ✅ New npm scripts added for project-specific testing

### Backward Compatibility

The shared config (`@cvix/config/vitest.config.shared`) is marked as deprecated but remains for any packages that might still reference it.
