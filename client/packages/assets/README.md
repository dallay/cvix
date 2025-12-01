# @cvix/assets

Centralized assets package for all CVIX frontend applications.

## Overview

This package serves as a **single source of truth** for shared assets across:

- `@cvix/marketing` (Astro landing page)
- `@cvix/webapp` (Vue.js web application)
- `@cvix/docs` (Starlight documentation)

## Structure

```text
├── public/                    # Static assets (favicons, manifest, PWA icons)
│   └── manifest.json          # PWA manifest
├── src/
│   ├── images/                # General images
│   │   ├── blog/              # Blog placeholder images
│   │   ├── cta-dashboard-mockup.svg
│   │   ├── cta-dashboard-mockup-dark.svg
│   │   ├── video-placeholder.png
│   │   └── pet.*              # Mascot images
│   ├── logos/                 # Brand logos
│   │   ├── light-isotype.svg
│   │   └── dark-isotype.svg
│   ├── icons/                 # Shared icons
│   └── index.ts               # Barrel export with asset path constants
├── package.json
├── tsconfig.json
└── README.md
```

## Usage

### Import via alias

Each app configures the `@cvix/assets` alias in their build tool configuration:

```typescript
// In your Vue/Astro component
import lightLogo from "@cvix/assets/logos/light-isotype.svg";
import placeholder from "@cvix/assets/images/blog/blog-placeholder-1.avif";
```

### Programmatic access to paths

```typescript
import { LOGOS, BLOG_PLACEHOLDERS, IMAGES } from "@cvix/assets";

// Use the path constants
console.log(LOGOS.light); // "logos/light-isotype.svg"
```

### Public static files

For files that need to be served from the `/public` directory (like `manifest.json`),
copy them during the build process:

```json
{
  "scripts": {
    "sync:assets": "cp -r ../../packages/assets/public/* ./public/"
  }
}
```

Or use a Vite plugin like `vite-plugin-static-copy`.

## Adding new assets

1. Add the asset file to the appropriate directory under `src/`
2. (Optional) Export the path in `src/index.ts` for programmatic access
3. Run `pnpm install` at the monorepo root to ensure the package is linked

## Configuration in consuming apps

### Astro (marketing, docs)

```mjs
// astro.config.mjs
import { fileURLToPath } from "node:url";

export default defineConfig({
  vite: {
    resolve: {
      alias: {
        "@cvix/assets": fileURLToPath(
          new URL("../../packages/assets/src", import.meta.url)
        ),
      },
    },
  },
});
```

### Vite (webapp)

```typescript
// vite.config.ts
import { fileURLToPath, URL } from "node:url";

export default defineConfig({
  resolve: {
    alias: {
      "@cvix/assets": fileURLToPath(
        new URL("../../packages/assets/src", import.meta.url)
      ),
    },
  },
});
```
