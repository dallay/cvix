# Quickstart: PDF Section Selector

**Feature**: `005-pdf-section-selector`
**Date**: 2025-12-06

## Prerequisites

- Node.js 20+ with pnpm 10.13.1+
- Docker (for backend services if running full stack)
- Access to Figma
  design: [cvix Figma](https://www.figma.com/design/RdLso6u4iuoulszrHaaraY/cvix?node-id=1-2)

## Quick Start

### 1. Start Development Environment

```bash
# Enter project directory and checkout feature branch
cd cvix
git checkout 005-pdf-section-selector

# Install dependencies
pnpm install

# Start backend services (if needed for PDF generation)
make start

# Start frontend dev server
cd client/apps/webapp
pnpm dev
```

### 2. Access Feature

Navigate to: **<http://localhost:9876/resume/pdf>**

You must have resume data loaded (either create via editor at `/resume/editor` or import JSON
Resume).

## Implementation Order

### Phase 1: Domain & Infrastructure (Foundation)

1. **Create SectionVisibility types** (`domain/SectionVisibility.ts`)
    - Define interfaces as documented in `data-model.md`
    - Add factory function `createDefaultVisibility`
    - Export section type constants

2. **Create Storage Service** (`infrastructure/storage/SectionVisibilityStorage.ts`)
    - Implement localStorage read/write with TTL
    - Add schema versioning for migrations

3. **Create Filter Service** (`application/ResumeSectionFilterService.ts`)
    - Implement `filterResume()` function
    - Unit test with various visibility configurations

### Phase 2: State Management

1. **Create Visibility Store** (`infrastructure/store/section-visibility.store.ts`)
    - Pinia store with visibility state
    - Actions: initialize, toggle, expand, reset
    - Watch for auto-disable when all items toggled off

### Phase 3: UI Components

1. **Create SectionTogglePill** (`presentation/components/SectionTogglePill.vue`)
    - Match Figma design exactly (see styling tokens in contracts)
    - Implement enabled/disabled states
    - Add accessibility attributes

2. **Create ItemToggleList** (`presentation/components/ItemToggleList.vue`)
    - Checkbox list for items within section
    - Handle Personal Details fields separately

3. **Create SectionTogglePanel** (`presentation/components/SectionTogglePanel.vue`)
    - Container for all pills
    - Wire up Collapsible for expand/collapse
    - Add "Add Custom Section" button (can be placeholder)

### Phase 4: Integration

1. **Update ResumePdfPage.vue**
    - Import and use visibility store
    - Add SectionTogglePanel above preview
    - Pass filtered resume to PDF generator

2. **Update ResumePreview (if applicable)**
    - Consume filtered resume for live preview

### Phase 5: Testing & Polish

1. **Unit Tests**
    - Filter service tests
    - Store action tests
    - Component tests with testing-library

2. **E2E Tests**
    - Toggle section flow
    - PDF generation with filtered content
    - Preference persistence

## Key Files to Create/Modify

| File                                                                                                   | Action | Priority |
| ------------------------------------------------------------------------------------------------------ | ------ | -------- |
| client/apps/webapp/src/core/resume/domain/SectionVisibility.ts                                         | CREATE | P1       |
| client/apps/webapp/src/core/resume/application/ResumeSectionFilterService.ts                           | CREATE | P1       |
| client/apps/webapp/src/core/resume/infrastructure/storage/SectionVisibilityStorage.ts                  | CREATE | P1       |
| client/apps/webapp/src/core/resume/infrastructure/store/section-visibility.store.ts                    | CREATE | P1       |
| client/apps/webapp/src/core/resume/infrastructure/presentation/components/SectionTogglePill.vue        | CREATE | P1       |
| client/apps/webapp/src/core/resume/infrastructure/presentation/components/ItemToggleList.vue           | CREATE | P1       |
| client/apps/webapp/src/core/resume/infrastructure/presentation/components/SectionTogglePanel.vue       | CREATE | P1       |
| client/apps/webapp/src/core/resume/infrastructure/presentation/components/PersonalDetailsFieldList.vue | CREATE | P1       |
| client/apps/webapp/src/core/resume/infrastructure/presentation/pages/ResumePdfPage.vue                 | MODIFY | P1       |
| client/apps/webapp/src/i18n/locale files                                                               | MODIFY | P1       |
| client/apps/webapp/src/core/resume/e2e tests                                                           | CREATE | P2       |

## Testing Commands

```bash
# Run all frontend tests
pnpm --filter webapp test

# Run specific test file
pnpm --filter webapp test SectionVisibility

# Run E2E tests
pnpm --filter webapp test:e2e

# Type checking
pnpm --filter webapp typecheck

# Lint, format, and type-check (recommended before PR)
pnpm check # Runs Biome lint, Biome format, and TypeScript type-checks for all frontend packages. This is the recommended pre-PR command. It ensures code style, formatting, and type safety. Run this before opening a pull request.

# Additional commands (if needed)
pnpm lint   # Runs only lint checks (Biome)
pnpm format # Runs only formatting (Biome)
pnpm test   # Runs all tests (unit, integration)
```

## Design Reference

### Figma Node IDs

- Main screen: `1:2`
- Section pills container: `1:94`
- Active pill (Personal Details): `1:95`
- Inactive pill (Projects): `1:115`

### Color Tokens (from globals.css)

- `--primary`: `oklch(0.541 0.281 293.009)` (purple)
- `--primary-foreground`: `oklch(0.969 0.016 293.756)` (light)
- `--muted`: `oklch(0.967 0.001 286.375)` (gray background)
- `--border`: `oklch(0.92 0.004 286.32)` (light gray border)

## Common Issues

### PDF Preview Not Updating

- Ensure `watch` on visibility is triggering
- Check that `filterResume` is called before `generatePdf`
- Verify debounce isn't too aggressive

### Preferences Not Persisting

- Check localStorage in DevTools → Application → Local Storage
- Verify key format: `cvix-section-visibility-{resumeId}`
- Check for JSON parse errors in console

### Section Pill States Not Matching Figma

- Compare exact Tailwind classes with contracts/component-contracts.md
- Ensure design tokens are being used, not hardcoded colors

## Related Documentation

- [Feature Specification](./spec.md)
- [Implementation Plan](./plan.md)
- [Research Notes](./research.md)
- [Data Model](./data-model.md)
- [API Contracts](./contracts/api-contracts.md)
- [Component Contracts](./contracts/component-contracts.md)
- [Design System](/.ruler/02_FRONTEND/05_DESIGN_SYSTEM.md)
- [Vue Conventions](/.ruler/02_FRONTEND/02_VUE_CONVENTIONS.md)
