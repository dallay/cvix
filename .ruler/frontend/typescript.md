# TypeScript Conventions

> Conventions and best practices for writing TypeScript code in the frontend codebase.

## General Style

- Use **Biome** for all linting and formatting. Run `pnpm check` before committing.
- Use 2 spaces for indentation
- Always use semicolons

## Types

- Prefer `type` over `interface` for defining object shapes, unless you need declaration merging
- **Always provide explicit types** for function arguments and return values
- **Avoid `any` at all costs**—use `unknown` for values where the type is truly unknown and perform type-checking
- Use TypeScript utility types (`Partial`, `Pick`, `Omit`, `Record`, etc.) to compose and transform types

## Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Files (TypeScript) | `kebab-case.ts` | `user-profile-card.ts` |
| Files (Vue) | `PascalCase.vue` | `UserProfileCard.vue` |
| Directories | `kebab-case` | `user-profile/`, `resume-generator/` |
| Types/Interfaces | `PascalCase` | `UserProfile`, `ResumeData` |
| Variables/Functions | `camelCase` | `userProfile`, `getResumeData` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_RESUME_LENGTH` |
| Composables | `useXxx` prefix | `useResumeForm.ts` |
| Stores (Pinia) | `useXxxStore` | `useUserStore.ts` |
| Components | `PascalCase.vue` | `ResumePreview.vue` |

## Functions

- Use arrow functions (`const fn = () => {}`) by default
- Keep functions small and focused on a single responsibility

## Imports & Exports

- Use ES module syntax (`import`/`export`)
- Use absolute path aliases (`@/` for `src/`) configured in `tsconfig.json`
- **Prefer named exports** over default exports to avoid naming inconsistencies

```typescript
// ✅ Good - named export
export const formatDate = (date: Date): string => { ... };

// ❌ Avoid - default export
export default function formatDate(date: Date): string { ... }
```

## Code Quality & Safety

- Enable `strict` mode in `tsconfig.json`
- Use `readonly` for properties and variables to enforce immutability where possible
- Use utility types (`Partial`, `Pick`, `Omit`, etc.) to create new types from existing ones
- Use `as const` to preserve literal types

```typescript
// ✅ Good - explicit types, readonly
type UserProfile = {
  readonly id: string;
  readonly name: string;
  email: string;
};

const STATUS = {
  ACTIVE: 'active',
  INACTIVE: 'inactive',
} as const;

type Status = typeof STATUS[keyof typeof STATUS];
```

## Type Safety Patterns

### Discriminated Unions

```typescript
type Result<T> =
  | { success: true; data: T }
  | { success: false; error: string };

const handleResult = <T>(result: Result<T>) => {
  if (result.success) {
    // TypeScript knows result.data exists
    console.log(result.data);
  } else {
    // TypeScript knows result.error exists
    console.error(result.error);
  }
};
```

### Type Guards

```typescript
const isUser = (value: unknown): value is User => {
  return (
    typeof value === 'object' &&
    value !== null &&
    'id' in value &&
    'name' in value
  );
};
```
