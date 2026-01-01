# @cvix/i18n

Localization and internationalization utilities, types, and configs for all CVIX frontend and backend applications.

## Features

- Locale-aware routing helpers.
- Shared translation keys and schemas (TypeScript-first).
- Locale switching logic for UI.
- Easy integration with Astro and Vue webapps.

## Structure

```text
src/
  locales/        # Per-locale JSON translation files and config
  astro/          # Astro-specific helpers and types
  types.ts        # Shared type definitions
  config.ts       # App-wide i18n configuration (default locale, supported languages)
index.ts
```

## Usage

```typescript
import { DEFAULT_LOCALE, LOCALES } from "@cvix/i18n"
import { formatI18nRoute } from "@cvix/i18n/astro"
```

## Adding Languages

- Add the translation file under `src/locales/`.
- Register it in `config.ts`.
- Use types for safety.

## Consuming app setup

- Configure the path alias "@cvix/i18n" in your build tool to point to `packages/i18n/src`.

---
