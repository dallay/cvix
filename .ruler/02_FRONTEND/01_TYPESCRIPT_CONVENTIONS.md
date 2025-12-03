# TypeScript Conventions

> This document defines the conventions and best practices for writing TypeScript code.

## General Style

- Use **Biome** for all linting and formatting. Run `pnpm check` before committing.
- Use 2 spaces for indentation and always use semicolons.

## Types

- Prefer `type` over `interface` for defining object shapes, unless you need declaration merging.
- Always provide explicit types for function arguments and return values.
- Avoid `any` at all costs. Use `unknown` for values where the type is truly unknown and perform type-checking.
- Use TypeScript utility types (`Partial`, `Pick`, `Omit`, `Record`, etc.) to compose and transform types for better reusability and clarity.

## Naming Conventions

All naming conventions in the frontend codebase (inside `client/`) follow these rules:

- **Files**: Use `kebab-case.ts` for all TypeScript files (e.g., `user-profile-card.ts`). Vue components use `PascalCase.vue` (e.g., `UserProfileCard.vue`).
- **Directories**: Use `kebab-case` (e.g., `user-profile/`, `resume-generator/`).
- **Types/Interfaces**: Use `PascalCase` (e.g., `UserProfile`, `ResumeData`).
- **Variables/Functions**: Use `camelCase` (e.g., `userProfile`, `getResumeData`).
- **Constants**: Use `UPPER_SNAKE_CASE` (e.g., `MAX_RESUME_LENGTH`).
- **Composables**: Use `useXxx` prefix in `camelCase` (e.g., `useResumeForm.ts`).
- **Stores (Pinia)**: Use `useXxxStore` in `camelCase` (e.g., `useUserStore.ts`).
- **Components**: Use `PascalCase.vue` (e.g., `ResumePreview.vue`).

These conventions ensure consistency, readability, and easy navigation in the monorepo frontend structure.

## Functions

- Use arrow functions (`const fn = () => {}`) by default.
- Keep functions small and focused on a single responsibility.

## Imports & Exports

- Use ES module syntax (`import`/`export`).
- Use absolute path aliases (`@/` for `src/`) configured in `tsconfig.json`.
- Prefer named exports over default exports to avoid naming inconsistencies.

## Code Quality & Safety

- Enable `strict` mode in `tsconfig.json`.
- Use `readonly` for properties and variables to enforce immutability where possible.
- Use utility types (`Partial`, `Pick`, `Omit`, etc.) to create new types from existing ones.
- Use `as const` to preserve literal types.
