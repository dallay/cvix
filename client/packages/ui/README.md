# @cvix/ui

Shared Vue 3 UI components and design primitives for CVIX apps (webapp, marketing, docs).

## Features

- Vue 3 components (composition API, typed)
- Design system tokens and theme variant logic
- Exposes all base UI primitives (Button, Input, Card, etc)
- Typed exports and full TypeScript support

## Structure

```text
src/
  components/
    Button.vue
    Card.vue
    Input.vue
    ...
  index.ts
```

## Usage

```js
// vite.config.ts (add alias if needed)
{
  resolve: {
    alias: {
      "@cvix/ui": "<project-root>/packages/ui/src"
    }
  }
}
// In your Vue SFC or setup script:
import { Button } from "@cvix/ui";
```

## Extending

- Prefer composition over inheritance – wrap base components if you need new variants.
- All styles use design system tokens for consistency.

---
