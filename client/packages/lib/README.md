# @cvix/lib

Shared TypeScript/Kotlin utility functions and business-agnostic helpers for CVIX apps.

## Features

- Utility functions (object, array, string manipulation)
- Class name helpers (`cn`, commonly used for class merging)
- Functional programming helpers
- Pure logic, no framework dependencies

## Usage

```typescript
import { cn } from "@cvix/lib";
import { someUtility } from "@cvix/lib";
```

## Structure

```text
src/
  index.ts
  ...other utils
```

## Best Practices

- Only include business-agnostic helpers (no I/O, UI or domain-specific code).
- Use in both Astro and Vue codebases, or anywhere in the monorepo.

---
